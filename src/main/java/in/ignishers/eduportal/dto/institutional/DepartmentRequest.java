package in.ignishers.eduportal.dto.institutional;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DepartmentRequest(
        @NotBlank(message = "College name is required")
        @Size(max = 150, message = "College name must not exceed 150 characters")
        String name,

        @NotBlank(message = "College code is required")
        @Size(max = 20, message = "College code must not exceed 20 characters")
        String code,

        @Email(message = "Please provide a valid HOD email address")
        String hodEmail
) {
}
