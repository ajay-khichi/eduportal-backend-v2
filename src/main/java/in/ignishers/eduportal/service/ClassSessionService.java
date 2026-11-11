package in.ignishers.eduportal.service;

import in.ignishers.eduportal.dto.attendance.ClassSessionRequest;
import in.ignishers.eduportal.dto.attendance.ClassSessionResponse;
import in.ignishers.eduportal.dto.attendance.ClassStatusUpdateRequest;
import in.ignishers.eduportal.dto.attendance.RecurringScheduleRequest;
import in.ignishers.eduportal.enums.ClassStatus;
import in.ignishers.eduportal.exception.custom.ResourceNotFoundException;
import in.ignishers.eduportal.models.attendance.ClassSession;
import in.ignishers.eduportal.models.curriculum.AcademicSession;
import in.ignishers.eduportal.models.people.TeachingAssignment;
import in.ignishers.eduportal.repos.attendance.ClassSessionRepository;
import in.ignishers.eduportal.repos.people.TeachingAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClassSessionService {

    private final ClassSessionRepository classSessionRepo;
    private final TeachingAssignmentRepository assignmentRepo;

    @Transactional
    public ClassSessionResponse scheduleClass(ClassSessionRequest request) {
        TeachingAssignment assignment = assignmentRepo.findById(request.teachingAssignmentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Teaching assignment not found with id: " + request.teachingAssignmentId()));

        String normalizedRoom = normalizeRoom(request.room());
        validateTimeSlot(request.startTime(), request.endTime());
        validateWithinAcademicSession(request.classDate(), assignment.getAcademicSession());
        checkConflicts(assignment, request.classDate(), request.startTime(), request.endTime(), normalizedRoom, null);

        ClassSession session = new ClassSession();
        session.setTeachingAssignment(assignment);
        session.setClassDate(request.classDate());
        session.setStartTime(request.startTime());
        session.setEndTime(request.endTime());
        session.setRoom(normalizedRoom);
        session.setStatus(ClassStatus.SCHEDULED);

        session = classSessionRepo.save(session);
        return toResponse(session);
    }

    @Transactional
    public List<ClassSessionResponse> scheduleRecurringClasses(RecurringScheduleRequest request) {
        TeachingAssignment assignment = assignmentRepo.findById(request.teachingAssignmentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Teaching assignment not found with id: " + request.teachingAssignmentId()));

        if (request.startDate().isAfter(request.endDate())) {
            throw new IllegalArgumentException("Schedule start date cannot be after end date");
        }

        validateTimeSlot(request.startTime(), request.endTime());
        validateWithinAcademicSession(request.startDate(), assignment.getAcademicSession());
        validateWithinAcademicSession(request.endDate(), assignment.getAcademicSession());

        String normalizedRoom = normalizeRoom(request.room());
        LocalDate current = request.startDate().with(TemporalAdjusters.nextOrSame(request.dayOfWeek()));

        List<ClassSession> sessionsToCreate = new ArrayList<>();
        while (!current.isAfter(request.endDate())) {
            checkConflicts(assignment, current, request.startTime(), request.endTime(), normalizedRoom, null);

            ClassSession session = new ClassSession();
            session.setTeachingAssignment(assignment);
            session.setClassDate(current);
            session.setStartTime(request.startTime());
            session.setEndTime(request.endTime());
            session.setRoom(normalizedRoom);
            session.setStatus(ClassStatus.SCHEDULED);

            sessionsToCreate.add(session);
            current = current.plusWeeks(1);
        }

        if (sessionsToCreate.isEmpty()) {
            throw new IllegalArgumentException("No matching dates found for " + request.dayOfWeek() + " in the given range");
        }

        return classSessionRepo.saveAll(sessionsToCreate)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ClassSessionResponse getClassSessionById(Long id) {
        ClassSession session = classSessionRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Class session not found with id: " + id));
        return toResponse(session);
    }

    @Transactional(readOnly = true)
    public List<ClassSessionResponse> getSectionTimetable(Long sectionId, LocalDate from, LocalDate to) {
        LocalDate start = from != null ? from : LocalDate.now();
        LocalDate end = to != null ? to : start.plusDays(7);
        return classSessionRepo
                .findAllByTeachingAssignmentSectionIdAndClassDateBetweenOrderByClassDateAscStartTimeAsc(sectionId, start, end)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ClassSessionResponse> getFacultyTimetable(Long facultyId, LocalDate from, LocalDate to) {
        LocalDate start = from != null ? from : LocalDate.now();
        LocalDate end = to != null ? to : start.plusDays(7);
        return classSessionRepo
                .findAllByTeachingAssignmentFacultyIdAndClassDateBetweenOrderByClassDateAscStartTimeAsc(facultyId, start, end)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ClassSessionResponse updateClassSession(Long id, ClassSessionRequest request) {
        ClassSession session = classSessionRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Class session not found with id: " + id));

        TeachingAssignment assignment = assignmentRepo.findById(request.teachingAssignmentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Teaching assignment not found with id: " + request.teachingAssignmentId()));

        String normalizedRoom = normalizeRoom(request.room());
        validateTimeSlot(request.startTime(), request.endTime());
        validateWithinAcademicSession(request.classDate(), assignment.getAcademicSession());
        checkConflicts(assignment, request.classDate(), request.startTime(), request.endTime(), normalizedRoom, id);

        session.setTeachingAssignment(assignment);
        session.setClassDate(request.classDate());
        session.setStartTime(request.startTime());
        session.setEndTime(request.endTime());
        session.setRoom(normalizedRoom);

        session = classSessionRepo.save(session);
        return toResponse(session);
    }

    @Transactional
    public ClassSessionResponse updateStatus(Long id, ClassStatusUpdateRequest request) {
        ClassSession session = classSessionRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Class session not found with id: " + id));

        session.setStatus(request.status());
        session = classSessionRepo.save(session);
        return toResponse(session);
    }

    private void validateTimeSlot(LocalTime startTime, LocalTime endTime) {
        if (!startTime.isBefore(endTime)) {
            throw new IllegalArgumentException("Start time must be strictly before end time");
        }
    }

    private void validateWithinAcademicSession(LocalDate date, AcademicSession academicSession) {
        if (date.isBefore(academicSession.getStartDate()) || date.isAfter(academicSession.getEndDate())) {
            throw new IllegalArgumentException(
                    "Class date " + date + " must fall within academic session '" + academicSession.getName()
                            + "' (" + academicSession.getStartDate() + " to " + academicSession.getEndDate() + ")");
        }
    }

    private void checkConflicts(
            TeachingAssignment assignment,
            LocalDate classDate,
            LocalTime startTime,
            LocalTime endTime,
            String room,
            Long excludeId
    ) {
        Long facultyId = assignment.getFaculty().getId();
        Long sectionId = assignment.getSection().getId();
        Long collegeId = assignment.getFaculty().getCollege().getId();

        if (classSessionRepo.existsFacultyConflict(facultyId, classDate, startTime, endTime, excludeId)) {
            throw new IllegalArgumentException(
                    "Schedule clash: Faculty is already booked on " + classDate + " between " + startTime + " and " + endTime);
        }

        if (classSessionRepo.existsSectionConflict(sectionId, classDate, startTime, endTime, excludeId)) {
            throw new IllegalArgumentException(
                    "Schedule clash: Section already has another class on " + classDate + " between " + startTime + " and " + endTime);
        }

        if (room != null && classSessionRepo.existsRoomConflict(collegeId, room, classDate, startTime, endTime, excludeId)) {
            throw new IllegalArgumentException(
                    "Schedule clash: Room '" + room + "' is already occupied on " + classDate + " between " + startTime + " and " + endTime);
        }
    }

    private String normalizeRoom(String room) {
        return (room != null && !room.isBlank()) ? room.trim() : null;
    }

    private ClassSessionResponse toResponse(ClassSession cs) {
        TeachingAssignment ta = cs.getTeachingAssignment();
        return new ClassSessionResponse(
                cs.getId(),
                ta.getId(),
                ta.getFaculty().getId(),
                ta.getFaculty().getFirstName() + (ta.getFaculty().getLastName() != null ? " " + ta.getFaculty().getLastName() : ""),
                ta.getSubject().getId(),
                ta.getSubject().getName(),
                ta.getSubject().getCode(),
                ta.getSection().getId(),
                ta.getSection().getName(),
                ta.getSection().getSemester(),
                cs.getClassDate(),
                cs.getStartTime(),
                cs.getEndTime(),
                cs.getRoom(),
                cs.getStatus(),
                cs.getCreatedAt()
        );
    }
}
