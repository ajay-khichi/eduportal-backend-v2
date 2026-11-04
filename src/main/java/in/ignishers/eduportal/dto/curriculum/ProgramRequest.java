package in.ignishers.eduportal.dto.curriculum;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProgramRequest(
        @NotBlank(message = "Program name is required")
        @Size(max = 100, message = "Program name must not exceed 100 characters")
        String name,
        @NotBlank(message = "Program code is required")
        @Size(max = 30, message = "Program code must not exceed 30 characters")
        String code
) {
}
