package in.ignishers.eduportal.service;

import in.ignishers.eduportal.dto.attendance.CompleteAttendanceRequest;
import in.ignishers.eduportal.dto.attendance.MarkAttendanceResponse;
import in.ignishers.eduportal.dto.attendance.VerifyQrRequest;
import in.ignishers.eduportal.dto.attendance.VerifyQrResponse;
import in.ignishers.eduportal.enums.AttendanceStatus;
import in.ignishers.eduportal.enums.VerificationStatus;
import in.ignishers.eduportal.models.attendance.Attendance;
import in.ignishers.eduportal.models.attendance.AttendanceSession;
import in.ignishers.eduportal.models.attendance.AttendanceVerification;
import in.ignishers.eduportal.models.attendance.ClassSession;
import in.ignishers.eduportal.models.attendance.FaceProfile;
import in.ignishers.eduportal.models.auth.User;
import in.ignishers.eduportal.models.curriculum.Section;
import in.ignishers.eduportal.models.people.Faculty;
import in.ignishers.eduportal.models.people.Student;
import in.ignishers.eduportal.models.people.TeachingAssignment;
import in.ignishers.eduportal.repos.attendance.AttendanceRepository;
import in.ignishers.eduportal.repos.attendance.AttendanceSessionRepository;
import in.ignishers.eduportal.repos.attendance.ClassSessionRepository;
import in.ignishers.eduportal.repos.attendance.FaceProfileRepository;
import in.ignishers.eduportal.repos.people.EnrollmentRepository;
import in.ignishers.eduportal.repos.people.StudentRepository;
import in.ignishers.eduportal.util.BiometricUtils;
import in.ignishers.eduportal.util.QrTokenUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {

    @Mock
    private AttendanceSessionRepository sessionRepo;
    @Mock
    private AttendanceRepository attendanceRepo;
    @Mock
    private ClassSessionRepository classSessionRepo;
    @Mock
    private StudentRepository studentRepo;
    @Mock
    private EnrollmentRepository enrollmentRepo;
    @Mock
    private FaceProfileRepository faceProfileRepo;

    @InjectMocks
    private AttendanceService attendanceService;

    private Student student;
    private AttendanceSession attendanceSession;
    private ClassSession classSession;
    private String sessionSecret;
    private List<Float> enrolledEmbedding;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(attendanceService, "qrStepSeconds", 5);
        ReflectionTestUtils.setField(attendanceService, "checkinWindowSeconds", 20);
        ReflectionTestUtils.setField(attendanceService, "faceSimilarityThreshold", 0.75);
        ReflectionTestUtils.setField(attendanceService, "defaultGeofenceRadiusMeters", 100);

        User studentUser = new User();
        studentUser.setId(200L);

        student = new Student();
        student.setId(20L);
        student.setUser(studentUser);
        student.setEnrollmentNumber("EN-2026-01");

        User facultyUser = new User();
        facultyUser.setId(100L);
        Faculty faculty = new Faculty();
        faculty.setId(10L);
        faculty.setUser(facultyUser);

        Section section = new Section();
        section.setId(30L);

        TeachingAssignment ta = new TeachingAssignment();
        ta.setId(40L);
        ta.setFaculty(faculty);
        ta.setSection(section);

        classSession = new ClassSession();
        classSession.setId(50L);
        classSession.setTeachingAssignment(ta);

        sessionSecret = QrTokenUtils.generateSecret();
        attendanceSession = new AttendanceSession();
        attendanceSession.setId(60L);
        attendanceSession.setClassSession(classSession);
        attendanceSession.setSecret(sessionSecret);
        attendanceSession.setStartedAt(LocalDateTime.now().minusMinutes(1));
        attendanceSession.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        attendanceSession.setLatitude(new BigDecimal("28.6139000"));
        attendanceSession.setLongitude(new BigDecimal("77.2090000"));
        attendanceSession.setRadiusMeters(100);
        attendanceSession.setActive(true);

        enrolledEmbedding = List.of(0.8f, 0.2f, 0.4f, 0.1f);
    }

    @Test
    void phase1_verifyQr_shouldIssueChallengeTokenAndSavePendingRecord() {
        String qrToken = QrTokenUtils.generateRollingToken(60L, sessionSecret, 5);

        when(sessionRepo.findById(60L)).thenReturn(Optional.of(attendanceSession));
        when(studentRepo.findByUserIdAndDeletedFalse(200L)).thenReturn(Optional.of(student));
        when(enrollmentRepo.existsByStudentIdAndSectionId(20L, 30L)).thenReturn(true);
        when(attendanceRepo.findByClassSessionIdAndStudentId(50L, 20L)).thenReturn(Optional.empty());
        when(attendanceRepo.save(any(Attendance.class))).thenAnswer(inv -> {
            Attendance saved = inv.getArgument(0);
            saved.setId(70L);
            return saved;
        });

        VerifyQrResponse response = attendanceService.verifyQrAndStartChallenge(200L, new VerifyQrRequest(qrToken));

        assertEquals(20, response.expiresInSeconds());
        QrTokenUtils.ChallengePayload payload = QrTokenUtils.parseChallengeToken(response.challengeToken());
        assertEquals(70L, payload.attendanceId());
        assertEquals(20L, payload.studentId());
        assertEquals(60L, payload.sessionId());
    }

    @Test
    void phase2_completeCheckIn_shouldRejectStolenChallengeTokenFromAnotherStudent() {
        String challenge = QrTokenUtils.generateChallengeToken(70L, 20L, 60L, 20, sessionSecret);

        User otherUser = new User();
        otherUser.setId(201L);
        Student otherStudent = new Student();
        otherStudent.setId(21L);
        otherStudent.setUser(otherUser);

        when(studentRepo.findByUserIdAndDeletedFalse(201L)).thenReturn(Optional.of(otherStudent));

        CompleteAttendanceRequest req = new CompleteAttendanceRequest(
                challenge,
                new BigDecimal("28.6139000"),
                new BigDecimal("77.2090000"),
                enrolledEmbedding
        );

        assertThrows(AccessDeniedException.class,
                () -> attendanceService.completeAttendanceCheckIn(201L, req));
    }

    @Test
    void phase2_completeCheckIn_shouldRejectOutsideGeofence() {
        String challenge = QrTokenUtils.generateChallengeToken(70L, 20L, 60L, 20, sessionSecret);
        Attendance pending = buildPendingAttendance();

        when(studentRepo.findByUserIdAndDeletedFalse(200L)).thenReturn(Optional.of(student));
        when(sessionRepo.findById(60L)).thenReturn(Optional.of(attendanceSession));
        when(attendanceRepo.findById(70L)).thenReturn(Optional.of(pending));

        // Coordinates ~1.1km away from classroom
        CompleteAttendanceRequest req = new CompleteAttendanceRequest(
                challenge,
                new BigDecimal("28.6239000"),
                new BigDecimal("77.2090000"),
                enrolledEmbedding
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> attendanceService.completeAttendanceCheckIn(200L, req));
        assertTrue(ex.getMessage().contains("outside classroom radius"));
    }

    @Test
    void phase2_completeCheckIn_shouldRejectMismatchedFaceBiometrics() {
        String challenge = QrTokenUtils.generateChallengeToken(70L, 20L, 60L, 20, sessionSecret);
        Attendance pending = buildPendingAttendance();
        FaceProfile faceProfile = buildFaceProfile();

        when(studentRepo.findByUserIdAndDeletedFalse(200L)).thenReturn(Optional.of(student));
        when(sessionRepo.findById(60L)).thenReturn(Optional.of(attendanceSession));
        when(attendanceRepo.findById(70L)).thenReturn(Optional.of(pending));
        when(faceProfileRepo.findByStudentIdAndActiveTrue(20L)).thenReturn(Optional.of(faceProfile));

        // Orthogonal / mismatched face vector
        CompleteAttendanceRequest req = new CompleteAttendanceRequest(
                challenge,
                new BigDecimal("28.6139500"),
                new BigDecimal("77.2090500"),
                List.of(-0.8f, 0.2f, -0.4f, 0.1f)
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> attendanceService.completeAttendanceCheckIn(200L, req));
        assertTrue(ex.getMessage().contains("Face verification failed"));
    }

    @Test
    void phase2_completeCheckIn_shouldMarkPresentWhenAllFactorsPass() {
        String challenge = QrTokenUtils.generateChallengeToken(70L, 20L, 60L, 20, sessionSecret);
        Attendance pending = buildPendingAttendance();
        FaceProfile faceProfile = buildFaceProfile();

        when(studentRepo.findByUserIdAndDeletedFalse(200L)).thenReturn(Optional.of(student));
        when(sessionRepo.findById(60L)).thenReturn(Optional.of(attendanceSession));
        when(attendanceRepo.findById(70L)).thenReturn(Optional.of(pending));
        when(faceProfileRepo.findByStudentIdAndActiveTrue(20L)).thenReturn(Optional.of(faceProfile));
        when(attendanceRepo.save(any(Attendance.class))).thenAnswer(inv -> inv.getArgument(0));

        CompleteAttendanceRequest req = new CompleteAttendanceRequest(
                challenge,
                new BigDecimal("28.6140000"),
                new BigDecimal("77.2090000"),
                enrolledEmbedding
        );

        MarkAttendanceResponse response = attendanceService.completeAttendanceCheckIn(200L, req);

        assertEquals(AttendanceStatus.PRESENT, response.status());
        assertEquals(VerificationStatus.VERIFIED, response.verificationStatus());
        assertTrue(response.qrVerified());
        assertTrue(response.locationVerified());
        assertTrue(response.faceVerified());
        assertEquals(1.0, response.faceSimilarity());
        assertTrue(response.distanceMeters() < 20.0);
    }

    private Attendance buildPendingAttendance() {
        Attendance pending = new Attendance();
        pending.setId(70L);
        pending.setClassSession(classSession);
        pending.setStudent(student);
        pending.setStatus(AttendanceStatus.ABSENT);

        AttendanceVerification verification = new AttendanceVerification();
        verification.setAttendance(pending);
        verification.setQrVerified(true);
        verification.setStatus(VerificationStatus.PENDING);
        pending.setVerification(verification);
        return pending;
    }

    private FaceProfile buildFaceProfile() {
        FaceProfile profile = new FaceProfile();
        profile.setStudent(student);
        profile.setEmbedding(BiometricUtils.toByteArray(enrolledEmbedding));
        profile.setModelVersion("mobilefacenet-v1");
        profile.setActive(true);
        return profile;
    }
}
