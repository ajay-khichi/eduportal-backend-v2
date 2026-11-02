package in.ignishers.eduportal.dto.user;

import in.ignishers.eduportal.enums.UserRole;

import java.time.LocalDateTime;

public record UserProfileResponse(
        String email,
        UserRole role,
        boolean active,
        LocalDateTime createdAt
) {
}
