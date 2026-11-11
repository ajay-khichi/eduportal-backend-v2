package in.ignishers.eduportal.dto.attendance;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

public record RecurringScheduleRequest(
        @NotNull(message = "Teaching assignment ID is required")
        Long teachingAssignmentId,

        @NotNull(message = "Day of week is required")
        DayOfWeek dayOfWeek,

        @NotNull(message = "Schedule start date is required")
        LocalDate startDate,

        @NotNull(message = "Schedule end date is required")
        LocalDate endDate,

        @NotNull(message = "Start time is required")
        LocalTime startTime,

        @NotNull(message = "End time is required")
        LocalTime endTime,

        @Size(max = 100, message = "Room name cannot exceed 100 characters")
        String room
) {}
