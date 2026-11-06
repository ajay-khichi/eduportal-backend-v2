package in.ignishers.eduportal.dto.curriculum;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record AcademicSessionRequest(
        @NotBlank(message = "Session name is required (e.g. 2025-2026)")
        @Size(max = 20, message = "Session name cannot exceed 20 characters")
        String name,

        @NotNull(message = "Start date is required")
        LocalDate startDate,

        @NotNull(message = "End date is required")
        LocalDate endDate,

        Boolean current
) {}