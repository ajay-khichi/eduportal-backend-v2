package in.ignishers.eduportal.dto.institutional;

import java.time.LocalDateTime;

public record CollegeResponse(
        Long id,
        String name,
        String code,
        String address,
        Long adminUserId,
        String adminEmail,
        LocalDateTime createdAt
) {
}
