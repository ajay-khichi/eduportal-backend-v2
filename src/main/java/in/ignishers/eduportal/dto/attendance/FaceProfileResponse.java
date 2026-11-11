package in.ignishers.eduportal.dto.attendance;

import java.time.LocalDateTime;

public record FaceProfileResponse(
        Long id,
        Long studentId,
        String enrollmentNumber,
        String studentName,
        String modelVersion,
        int embeddingDimension,
        boolean active,
        LocalDateTime enrolledAt
) {}
