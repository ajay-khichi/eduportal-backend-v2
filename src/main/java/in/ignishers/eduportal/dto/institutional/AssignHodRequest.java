package in.ignishers.eduportal.dto.institutional;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public record AssignHodRequest(
        @NotNull(message = "Admin user ID is required")
        @Email(message = "Please provide a valid HOD email address")
        String hodEmail
) {
}
