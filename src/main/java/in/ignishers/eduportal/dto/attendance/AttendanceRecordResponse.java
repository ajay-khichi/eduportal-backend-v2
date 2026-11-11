package in.ignishers.eduportal.dto.attendance;

import in.ignishers.eduportal.enums.AttendanceStatus;
import in.ignishers.eduportal.enums.VerificationStatus;

import java.time.LocalDateTime;

public record AttendanceRecordResponse(
        Long id,
        Long classSessionId,
        Long studentId,
        String enrollmentNumber,
        String studentName,
        AttendanceStatus status,
        VerificationStatus verificationStatus,
        boolean qrVerified,
        boolean faceVerified,
        LocalDateTime markedAt
) {}
