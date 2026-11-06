package in.ignishers.eduportal.dto.people;

import in.ignishers.eduportal.enums.FacultyRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record FacultyRequest(
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        String password,

        @NotNull(message = "Department ID is required")
        Long departmentId,

        @NotBlank(message = "Employee number is required")
        @Size(max = 50, message = "Employee number cannot exceed 50 characters")
        String employeeNumber,

        @NotBlank(message = "First name is required")
        @Size(max = 100, message = "First name cannot exceed 100 characters")
        String firstName,

        @Size(max = 100, message = "Last name cannot exceed 100 characters")
        String lastName,

        @Size(max = 20, message = "Phone cannot exceed 20 characters")
        String phone,

        @Size(max = 100, message = "Designation cannot exceed 100 characters")
        String designation,

        FacultyRole role
) {}