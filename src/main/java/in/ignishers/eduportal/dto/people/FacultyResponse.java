package in.ignishers.eduportal.dto.people;

import in.ignishers.eduportal.enums.FacultyRole;

import java.time.LocalDateTime;

public record FacultyResponse(
        Long id,
        Long userId,
        String email,
        Long collegeId,
        String collegeName,
        Long departmentId,
        String departmentName,
        String employeeNumber,
        String firstName,
        String lastName,
        String phone,
        String designation,
        FacultyRole role,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}