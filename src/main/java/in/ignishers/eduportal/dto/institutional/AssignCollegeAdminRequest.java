package in.ignishers.eduportal.dto.institutional;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AssignCollegeAdminRequest(
        @NotBlank(message = "Admin email is required")
        @Email(message = "Please enter a valid email address")
        String adminEmail
) {
}
