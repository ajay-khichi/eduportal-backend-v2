package in.ignishers.eduportal.dto.curriculum;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SectionRequest(
        @NotNull(message = "DepartmentProgram ID is required")
        Long departmentProgramId,

        @NotNull(message = "Academic Session ID is required")
        Long academicSessionId,

        @NotNull(message = "Semester is required")
        @Min(value = 1, message = "Semester must be at least 1")
        @Max(value = 12, message = "Semester cannot exceed 12")
        Integer semester,

        @NotBlank(message = "Section name is required (e.g. A, B, Batch-1)")
        @Size(max = 20, message = "Section name cannot exceed 20 characters")
        String name
) {}