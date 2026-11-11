package in.ignishers.eduportal.dto.people;

import java.time.LocalDateTime;

public record EnrollmentResponse(
        Long id,
        Long studentId,
        String studentName,
        String enrollmentNumber,
        String studentEmail,
        Long sectionId,
        String sectionName,
        Integer semester,
        String programName,
        Long academicSessionId,
        String academicSessionName,
        LocalDateTime enrolledAt
) {}