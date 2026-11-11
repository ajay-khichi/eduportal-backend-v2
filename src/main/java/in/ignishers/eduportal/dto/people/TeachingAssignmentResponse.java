package in.ignishers.eduportal.dto.people;

import java.time.LocalDateTime;

public record TeachingAssignmentResponse(
        Long id,
        Long facultyId,
        String facultyName,
        String employeeNumber,
        Long subjectId,
        String subjectName,
        String subjectCode,
        Long sectionId,
        String sectionName,
        Integer semester,
        Long academicSessionId,
        String academicSessionName,
        LocalDateTime assignedAt
) {}