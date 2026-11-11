package in.ignishers.eduportal.service;

import in.ignishers.eduportal.dto.attendance.*;
import in.ignishers.eduportal.enums.AttendanceStatus;
import in.ignishers.eduportal.enums.ClassStatus;
import in.ignishers.eduportal.enums.VerificationStatus;
import in.ignishers.eduportal.exception.custom.ResourceNotFoundException;
import in.ignishers.eduportal.models.attendance.Attendance;
import in.ignishers.eduportal.models.attendance.AttendanceSession;
import in.ignishers.eduportal.models.attendance.AttendanceVerification;
import in.ignishers.eduportal.models.attendance.ClassSession;
import in.ignishers.eduportal.models.attendance.FaceProfile;
import in.ignishers.eduportal.models.people.Student;
import in.ignishers.eduportal.repos.attendance.AttendanceRepository;
import in.ignishers.eduportal.repos.attendance.AttendanceSessionRepository;
import in.ignishers.eduportal.repos.attendance.ClassSessionRepository;
import in.ignishers.eduportal.repos.attendance.FaceProfileRepository;
import in.ignishers.eduportal.repos.people.EnrollmentRepository;
import in.ignishers.eduportal.repos.people.StudentRepository;
import in.ignishers.eduportal.util.BiometricUtils;
import in.ignishers.eduportal.util.GeoUtils;
import in.ignishers.eduportal.util.QrTokenUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceSessionRepository sessionRepo;
    private final AttendanceRepository attendanceRepo;
    private final ClassSessionRepository classSessionRepo;
    private final StudentRepository studentRepo;
    private final EnrollmentRepository enrollmentRepo;
    private final FaceProfileRepository faceProfileRepo;

    @Value("${app.attendance.qr-step-seconds:5}")
    private int qrStepSeconds;

    @Value("${app.attendance.face-similarity-threshold:0.75}")
    private double faceSimilarityThreshold;

    @Value("${app.attendance.default-geofence-radius-meters:100}")
    private int defaultGeofenceRadiusMeters;

    @Transactional
    public AttendanceSessionResponse startSession(
            Long classSessionId,
            Long actorUserId,
            boolean isAdmin,
            StartAttendanceSessionRequest request
    ) {
        ClassSession classSession = classSessionRepo.findById(classSessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Class session not found with id: " + classSessionId));

        verifyFacultyOwnership(classSession, actorUserId, isAdmin);

        if (classSession.getStatus() == ClassStatus.CANCELLED || classSession.getStatus() == ClassStatus.COMPLETED) {
            throw new IllegalArgumentException("Cannot start attendance for a " + classSession.getStatus() + " class");
        }

        validateCoordinatesPair(request.latitude(), request.longitude());

        LocalDateTime now = LocalDateTime.now();
        AttendanceSession session = sessionRepo.findByClassSessionId(classSessionId)
                .orElseGet(() -> {
                    AttendanceSession created = new AttendanceSession();
                    created.setClassSession(classSession);
                    return created;
                });

        if (session.getId() != null && session.isActive() && now.isBefore(session.getExpiresAt())) {
            throw new IllegalArgumentException("An active attendance session already exists for this class");
        }

        session.setSecret(QrTokenUtils.generateSecret());
        session.setStartedAt(now);
        session.setExpiresAt(now.plusMinutes(request.durationMinutes()));
        session.setLatitude(request.latitude());
        session.setLongitude(request.longitude());
        session.setRadiusMeters(resolveRadius(request.latitude(), request.radiusMeters()));
        session.setActive(true);

        classSession.setStatus(ClassStatus.ONGOING);
        classSessionRepo.save(classSession);

        session = sessionRepo.save(session);
        return toSessionResponse(session);
    }

    /**
     * Ultra-fast endpoint polled every ~5 seconds by the faculty client to display the rolling QR code.
     */
    @Transactional(readOnly = true)
    public QrTokenResponse getRollingQrToken(Long sessionId, Long actorUserId, boolean isAdmin) {
        AttendanceSessionRepository.QrSessionProjection session = sessionRepo.findQrProjectionById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance session not found with id: " + sessionId));

        if (!isAdmin && !session.getFacultyUserId().equals(actorUserId)) {
            throw new AccessDeniedException("Only the assigned faculty can generate QR tokens for this session");
        }

        if (!session.isActive() || LocalDateTime.now().isAfter(session.getExpiresAt())) {
            throw new IllegalArgumentException("Attendance session is closed or expired");
        }

        String token = QrTokenUtils.generateRollingToken(session.getId(), session.getSecret(), qrStepSeconds);
        int remaining = QrTokenUtils.secondsRemaining(qrStepSeconds);
        return new QrTokenResponse(token, remaining);
    }

    @Transactional
    public AttendanceSessionResponse stopSession(Long sessionId, Long actorUserId, boolean isAdmin) {
        AttendanceSession session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance session not found with id: " + sessionId));

        verifyFacultyOwnership(session.getClassSession(), actorUserId, isAdmin);

        session.setActive(false);
        session.getClassSession().setStatus(ClassStatus.COMPLETED);
        sessionRepo.save(session);

        return toSessionResponse(session);
    }

    @Transactional
    public MarkAttendanceResponse markAttendance(Long studentUserId, MarkAttendanceRequest request) {
        Long sessionId = QrTokenUtils.extractSessionId(request.qrToken());
        AttendanceSession session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance session not found"));

        LocalDateTime now = LocalDateTime.now();
        if (!session.isActive() || now.isAfter(session.getExpiresAt())) {
            throw new IllegalArgumentException("Attendance session is closed or expired");
        }

        Student student = studentRepo.findByUserIdAndDeletedFalse(studentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found for current user"));

        ClassSession classSession = session.getClassSession();
        Long sectionId = classSession.getTeachingAssignment().getSection().getId();
        if (!enrollmentRepo.existsByStudentIdAndSectionId(student.getId(), sectionId)) {
            throw new AccessDeniedException("You are not enrolled in this class section");
        }

        if (attendanceRepo.existsByClassSessionIdAndStudentId(classSession.getId(), student.getId())) {
            throw new IllegalArgumentException("Attendance is already marked for this class");
        }

        // Factor 1: Rolling 5-second QR verification
        if (!QrTokenUtils.verifyRollingToken(session.getId(), session.getSecret(), request.qrToken(), qrStepSeconds)) {
            throw new IllegalArgumentException("QR code is invalid or expired. Please scan the live QR again");
        }

        // Factor 2: GPS Geofence (Haversine)
        Double distanceMeters = verifyGeofence(session, request.latitude(), request.longitude());

        // Factor 3: Face Biometrics (Cosine Similarity)
        double similarity = verifyFaceBiometrics(student.getId(), request.faceEmbedding());

        Attendance attendance = new Attendance();
        attendance.setClassSession(classSession);
        attendance.setStudent(student);
        attendance.setStatus(AttendanceStatus.PRESENT);
        attendance.setMarkedAt(now);

        AttendanceVerification verification = new AttendanceVerification();
        verification.setAttendance(attendance);
        verification.setQrVerified(true);
        verification.setLatitude(request.latitude());
        verification.setLongitude(request.longitude());
        verification.setFaceVerified(true);
        verification.setStatus(VerificationStatus.VERIFIED);
        verification.setVerifiedAt(now);

        attendance.setVerification(verification);
        attendance = attendanceRepo.save(attendance);

        return new MarkAttendanceResponse(
                attendance.getId(),
                classSession.getId(),
                attendance.getStatus(),
                verification.getStatus(),
                true,
                true,
                true,
                distanceMeters,
                Math.round(similarity * 10000.0) / 10000.0,
                now
        );
    }

    @Transactional
    public AttendanceRecordResponse markManualAttendance(
            Long classSessionId,
            Long actorUserId,
            boolean isAdmin,
            ManualAttendanceRequest request
    ) {
        ClassSession classSession = classSessionRepo.findById(classSessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Class session not found with id: " + classSessionId));

        verifyFacultyOwnership(classSession, actorUserId, isAdmin);

        Student student = studentRepo.findByIdAndDeletedFalse(request.studentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + request.studentId()));

        Long sectionId = classSession.getTeachingAssignment().getSection().getId();
        if (!enrollmentRepo.existsByStudentIdAndSectionId(student.getId(), sectionId)) {
            throw new IllegalArgumentException("Student is not enrolled in this class section");
        }

        LocalDateTime now = LocalDateTime.now();
        Attendance attendance = attendanceRepo.findByClassSessionIdAndStudentId(classSessionId, student.getId())
                .orElseGet(() -> {
                    Attendance created = new Attendance();
                    created.setClassSession(classSession);
                    created.setStudent(student);
                    return created;
                });

        attendance.setStatus(request.status());
        attendance.setMarkedAt(now);

        AttendanceVerification verification = attendance.getVerification();
        if (verification == null) {
            verification = new AttendanceVerification();
            verification.setAttendance(attendance);
            attendance.setVerification(verification);
        }
        verification.setStatus(VerificationStatus.VERIFIED);
        verification.setVerifiedAt(now);

        attendance = attendanceRepo.save(attendance);
        return toRecordResponse(attendance);
    }

    @Transactional(readOnly = true)
    public List<AttendanceRecordResponse> getClassAttendance(Long classSessionId, Long actorUserId, boolean isAdmin) {
        ClassSession classSession = classSessionRepo.findById(classSessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Class session not found with id: " + classSessionId));

        verifyFacultyOwnership(classSession, actorUserId, isAdmin);

        return attendanceRepo.findAllByClassSessionIdOrderByStudentEnrollmentNumberAsc(classSessionId)
                .stream()
                .map(this::toRecordResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SubjectAttendanceSummaryResponse> getMyAttendanceSummary(Long studentUserId, Long academicSessionId) {
        Student student = studentRepo.findByUserIdAndDeletedFalse(studentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found for current user"));
        return getStudentAttendanceSummary(student.getId(), academicSessionId);
    }

    @Transactional(readOnly = true)
    public List<SubjectAttendanceSummaryResponse> getStudentAttendanceSummary(Long studentId, Long academicSessionId) {
        if (!studentRepo.existsById(studentId)) {
            throw new ResourceNotFoundException("Student not found with id: " + studentId);
        }
        return attendanceRepo.getStudentAttendanceStatsBySession(studentId, academicSessionId)
                .stream()
                .map(stat -> {
                    long total = stat.getTotalClasses() != null ? stat.getTotalClasses() : 0L;
                    long attended = stat.getAttendedClasses() != null ? stat.getAttendedClasses() : 0L;
                    double pct = total > 0 ? Math.round((attended * 10000.0) / total) / 100.0 : 0.0;
                    return new SubjectAttendanceSummaryResponse(
                            stat.getSubjectId(),
                            stat.getSubjectName(),
                            stat.getSubjectCode(),
                            total,
                            attended,
                            pct
                    );
                })
                .toList();
    }

    private Double verifyGeofence(AttendanceSession session, BigDecimal studentLat, BigDecimal studentLon) {
        if (session.getLatitude() == null || session.getLongitude() == null) {
            return null;
        }
        if (studentLat == null || studentLon == null) {
            throw new IllegalArgumentException("GPS coordinates are required for this geofenced session");
        }
        int allowedRadius = session.getRadiusMeters() != null ? session.getRadiusMeters() : defaultGeofenceRadiusMeters;
        double distance = GeoUtils.calculateDistanceMeters(
                session.getLatitude(), session.getLongitude(),
                studentLat, studentLon
        );
        if (distance > allowedRadius) {
            throw new IllegalArgumentException(
                    "Location outside classroom radius (" + Math.round(distance) + "m away, max allowed " + allowedRadius + "m)");
        }
        return Math.round(distance * 100.0) / 100.0;
    }

    private double verifyFaceBiometrics(Long studentId, List<Float> liveEmbedding) {
        FaceProfile profile = faceProfileRepo.findByStudentIdAndActiveTrue(studentId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No active face profile registered. Please enroll your face before marking attendance"));

        float[] storedVector = BiometricUtils.toFloatArray(profile.getEmbedding());
        double similarity = BiometricUtils.cosineSimilarity(storedVector, liveEmbedding);
        if (similarity < faceSimilarityThreshold) {
            throw new IllegalArgumentException("Face verification failed. Live face does not match registered profile");
        }
        return similarity;
    }

    private void verifyFacultyOwnership(ClassSession classSession, Long actorUserId, boolean isAdmin) {
        if (isAdmin) {
            return;
        }
        Long assignedFacultyUserId = classSession.getTeachingAssignment().getFaculty().getUser().getId();
        if (!assignedFacultyUserId.equals(actorUserId)) {
            throw new AccessDeniedException("Only the assigned faculty can manage attendance for this class");
        }
    }

    private void validateCoordinatesPair(BigDecimal lat, BigDecimal lon) {
        if ((lat == null) != (lon == null)) {
            throw new IllegalArgumentException("Both latitude and longitude must be provided together");
        }
    }

    private Integer resolveRadius(BigDecimal lat, Integer requestedRadius) {
        if (lat == null) {
            return null;
        }
        return requestedRadius != null ? requestedRadius : defaultGeofenceRadiusMeters;
    }

    private AttendanceSessionResponse toSessionResponse(AttendanceSession s) {
        return new AttendanceSessionResponse(
                s.getId(),
                s.getClassSession().getId(),
                s.getStartedAt(),
                s.getExpiresAt(),
                s.isActive(),
                s.getLatitude() != null && s.getLongitude() != null,
                s.getRadiusMeters()
        );
    }

    private AttendanceRecordResponse toRecordResponse(Attendance a) {
        Student s = a.getStudent();
        AttendanceVerification v = a.getVerification();
        return new AttendanceRecordResponse(
                a.getId(),
                a.getClassSession().getId(),
                s.getId(),
                s.getEnrollmentNumber(),
                s.getFirstName() + (s.getLastName() != null ? " " + s.getLastName() : ""),
                a.getStatus(),
                v != null ? v.getStatus() : VerificationStatus.PENDING,
                v != null && v.isQrVerified(),
                v != null && v.isFaceVerified(),
                a.getMarkedAt()
        );
    }
}
