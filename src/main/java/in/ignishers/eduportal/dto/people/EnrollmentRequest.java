package in.ignishers.eduportal.dto.people;

import jakarta.validation.constraints.NotNull;

public record EnrollmentRequest(
        @NotNull(message = "Student ID is required")
        Long studentId
) {}