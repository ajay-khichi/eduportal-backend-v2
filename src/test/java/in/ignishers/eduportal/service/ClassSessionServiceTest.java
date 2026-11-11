package in.ignishers.eduportal.service;

import in.ignishers.eduportal.dto.attendance.ClassSessionRequest;
import in.ignishers.eduportal.dto.attendance.ClassSessionResponse;
import in.ignishers.eduportal.dto.attendance.RecurringScheduleRequest;
import in.ignishers.eduportal.enums.ClassStatus;
import in.ignishers.eduportal.models.attendance.ClassSession;
import in.ignishers.eduportal.models.curriculum.AcademicSession;
import in.ignishers.eduportal.models.curriculum.Section;
import in.ignishers.eduportal.models.curriculum.Subject;
import in.ignishers.eduportal.models.institutional.College;
import in.ignishers.eduportal.models.people.Faculty;
import in.ignishers.eduportal.models.people.TeachingAssignment;
import in.ignishers.eduportal.repos.attendance.ClassSessionRepository;
import in.ignishers.eduportal.repos.people.TeachingAssignmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClassSessionServiceTest {

    @Mock
    private ClassSessionRepository classSessionRepo;

    @Mock
    private TeachingAssignmentRepository assignmentRepo;

    @InjectMocks
    private ClassSessionService classSessionService;

    private TeachingAssignment assignment;

    @BeforeEach
    void setUp() {
        College college = new College();
        college.setId(1L);
        college.setName("Tech Institute");

        Faculty faculty = new Faculty();
        faculty.setId(10L);
        faculty.setFirstName("Anjali");
        faculty.setLastName("Sharma");
        faculty.setCollege(college);

        Subject subject = new Subject();
        subject.setId(20L);
        subject.setName("Operating Systems");
        subject.setCode("CS-501");

        AcademicSession academicSession = new AcademicSession();
        academicSession.setId(30L);
        academicSession.setName("2026-2027");
        academicSession.setStartDate(LocalDate.of(2026, 7, 1));
        academicSession.setEndDate(LocalDate.of(2026, 12, 31));

        Section section = new Section();
        section.setId(40L);
        section.setName("A");
        section.setSemester(5);
        section.setAcademicSession(academicSession);

        assignment = new TeachingAssignment();
        assignment.setId(50L);
        assignment.setFaculty(faculty);
        assignment.setSubject(subject);
        assignment.setSection(section);
        assignment.setAcademicSession(academicSession);
    }

    @Test
    void scheduleClass_shouldSucceedWhenNoConflictsExist() {
        LocalDate date = LocalDate.of(2026, 9, 10);
        LocalTime start = LocalTime.of(9, 0);
        LocalTime end = LocalTime.of(10, 0);

        when(assignmentRepo.findById(50L)).thenReturn(Optional.of(assignment));
        when(classSessionRepo.existsFacultyConflict(10L, date, start, end, null)).thenReturn(false);
        when(classSessionRepo.existsSectionConflict(40L, date, start, end, null)).thenReturn(false);
        when(classSessionRepo.existsRoomConflict(1L, "LH-101", date, start, end, null)).thenReturn(false);
        when(classSessionRepo.save(any(ClassSession.class))).thenAnswer(inv -> {
            ClassSession saved = inv.getArgument(0);
            saved.setId(100L);
            return saved;
        });

        ClassSessionResponse response = classSessionService.scheduleClass(
                new ClassSessionRequest(50L, date, start, end, " LH-101 ")
        );

        assertEquals(100L, response.id());
        assertEquals("LH-101", response.room());
        assertEquals(ClassStatus.SCHEDULED, response.status());
    }

    @Test
    void scheduleClass_shouldRejectFacultyConflict() {
        LocalDate date = LocalDate.of(2026, 9, 10);
        LocalTime start = LocalTime.of(9, 0);
        LocalTime end = LocalTime.of(10, 0);

        when(assignmentRepo.findById(50L)).thenReturn(Optional.of(assignment));
        when(classSessionRepo.existsFacultyConflict(10L, date, start, end, null)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> classSessionService.scheduleClass(new ClassSessionRequest(50L, date, start, end, "LH-101")));

        assertTrue(ex.getMessage().contains("Faculty is already booked"));
        verify(classSessionRepo, never()).save(any());
    }

    @Test
    void scheduleClass_shouldRejectDateOutsideAcademicSession() {
        LocalDate outOfTerm = LocalDate.of(2027, 2, 1);
        when(assignmentRepo.findById(50L)).thenReturn(Optional.of(assignment));

        assertThrows(IllegalArgumentException.class, () -> classSessionService.scheduleClass(
                new ClassSessionRequest(50L, outOfTerm, LocalTime.of(9, 0), LocalTime.of(10, 0), "LH-101")
        ));
    }

    @Test
    void scheduleRecurringClasses_shouldGenerateAllWeeklyOccurrences() {
        LocalDate from = LocalDate.of(2026, 9, 7); // Monday
        LocalDate to = LocalDate.of(2026, 9, 28);  // Monday (4 Mondays total)
        LocalTime start = LocalTime.of(10, 0);
        LocalTime end = LocalTime.of(11, 0);

        when(assignmentRepo.findById(50L)).thenReturn(Optional.of(assignment));
        when(classSessionRepo.existsFacultyConflict(anyLong(), any(), any(), any(), isNull())).thenReturn(false);
        when(classSessionRepo.existsSectionConflict(anyLong(), any(), any(), any(), isNull())).thenReturn(false);
        when(classSessionRepo.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        List<ClassSessionResponse> responses = classSessionService.scheduleRecurringClasses(
                new RecurringScheduleRequest(50L, DayOfWeek.MONDAY, from, to, start, end, null)
        );

        assertEquals(4, responses.size());
        assertEquals(LocalDate.of(2026, 9, 7), responses.get(0).classDate());
        assertEquals(LocalDate.of(2026, 9, 28), responses.get(3).classDate());
    }
}
