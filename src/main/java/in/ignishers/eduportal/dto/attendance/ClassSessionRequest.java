package in.ignishers.eduportal.dto.attendance;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;

public record ClassSessionRequest(
        @NotNull(message = "Teaching assignment ID is required")
        Long teachingAssignmentId,

        @NotNull(message = "Class date is required")
        LocalDate classDate,

        @NotNull(message = "Start time is required")
        LocalTime startTime,

        @NotNull(message = "End time is required")
        LocalTime endTime,

        @Size(max = 100, message = "Room name cannot exceed 100 characters")
        String room
) {}
