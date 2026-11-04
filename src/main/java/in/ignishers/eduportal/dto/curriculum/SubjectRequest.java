package in.ignishers.eduportal.dto.curriculum;

import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

public record SubjectRequest(
        @NotBlank(message = "Subject name is required")
        @Size(max = 150, message = "Subject name must not exceed 150 characters")
        String name,
        @NotBlank(message = "Subject code is required")
        @Size(max = 30, message = "Subject code must not exceed 30 characters")
        String code,
        @NotNull(message = "Semester is required")
        @Min(value = 1, message = "Semester must be at least 1")
        @Max(value = 12, message = "Semester cannot exceed 12")
        Integer semester,
        @NotNull(message = "Credits are required")
        @Min(value = 1, message = "Credits must be at least 1")
        @Max(value = 10, message = "Credits cannot exceed 10")
        Integer credits
) {
}
