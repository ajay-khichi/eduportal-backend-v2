package in.ignishers.eduportal.dto.curriculum;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record AcademicSessionResponse(
        Long id,
        Long collegeId,
        String collegeName,
        String name,
        LocalDate startDate,
        LocalDate endDate,
        boolean current,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}