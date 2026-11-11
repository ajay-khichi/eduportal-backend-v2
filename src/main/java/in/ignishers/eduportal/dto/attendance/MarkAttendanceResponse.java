package in.ignishers.eduportal.dto.attendance;

import in.ignishers.eduportal.enums.AttendanceStatus;
import in.ignishers.eduportal.enums.VerificationStatus;

import java.time.LocalDateTime;

public record MarkAttendanceResponse(
        Long attendanceId,
        Long classSessionId,
        AttendanceStatus status,
        VerificationStatus verificationStatus,
        boolean qrVerified,
        boolean locationVerified,
        boolean faceVerified,
        Double distanceMeters,
        Double faceSimilarity,
        LocalDateTime markedAt
) {}
