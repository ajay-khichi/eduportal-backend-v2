package in.ignishers.eduportal.dto.people;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record StudentRequest(
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        String password,

        @NotNull(message = "Department ID is required")
        Long departmentId,

        @NotBlank(message = "Enrollment number is required")
        @Size(max = 50, message = "Enrollment number cannot exceed 50 characters")
        String enrollmentNumber,

        @NotBlank(message = "First name is required")
        @Size(max = 100, message = "First name cannot exceed 100 characters")
        String firstName,

        @Size(max = 100, message = "Last name cannot exceed 100 characters")
        String lastName,

        @Size(max = 20, message = "Phone cannot exceed 20 characters")
        String phone,

        @NotNull(message = "Admission year is required")
        @Min(value = 2000, message = "Admission year must be valid")
        Integer admissionYear
) {}