package in.ignishers.eduportal.dto.attendance;

import java.time.LocalDateTime;

public record AttendanceSessionResponse(
        Long id,
        Long classSessionId,
        LocalDateTime startedAt,
        LocalDateTime expiresAt,
        boolean active,
        boolean geofenced,
        Integer radiusMeters
) {}
