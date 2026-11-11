package in.ignishers.eduportal.dto.people;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record BulkEnrollmentRequest(
        @NotEmpty(message = "Student ID list cannot be empty")
        List<Long> studentIds
) {}