package in.ignishers.eduportal.dto.curriculum;

import java.time.LocalDateTime;

public record ProgramResponse(
        Long id,
        String name,
        String code,
        LocalDateTime createdAt
) {
}
