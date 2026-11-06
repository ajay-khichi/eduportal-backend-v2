package in.ignishers.eduportal.dto.people;

import java.time.LocalDateTime;

public record StudentResponse(
        Long id,
        Long userId,
        String email,
        Long collegeId,
        String collegeName,
        Long departmentId,
        String departmentName,
        String enrollmentNumber,
        String firstName,
        String lastName,
        String phone,
        Integer admissionYear,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}