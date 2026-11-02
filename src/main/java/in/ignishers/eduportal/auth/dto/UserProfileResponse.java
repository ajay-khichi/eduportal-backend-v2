package in.ignishers.eduportal.auth.dto;

import in.ignishers.eduportal.enums.UserRole;

import java.time.LocalDateTime;

public record UserProfileResponse(
        String email,
        UserRole role,
        boolean active,
        LocalDateTime createdAt
) {
}
