package in.ignishers.eduportal.dto.attendance;

import in.ignishers.eduportal.enums.AttendanceStatus;
import jakarta.validation.constraints.NotNull;

public record ManualAttendanceRequest(
        @NotNull(message = "Student ID is required")
        Long studentId,

        @NotNull(message = "Attendance status is required")
        AttendanceStatus status
) {}
