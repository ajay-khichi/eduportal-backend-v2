package in.ignishers.eduportal.dto.institutional;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CollegeRequest(
        @NotBlank(message = "College name is required")
        @Size(max = 200, message = "College name must not exceed 200 characters")
        String name,

        @NotBlank(message = "College code is required")
        @Size(max = 30, message = "College code must not exceed 30 characters")
        String code,


        @Size(max = 255, message = "Address must not exceed 255 characters")
        String address,

        @Email(message = "Please provide a valid email address")
        String adminEmail
        ) {
}
