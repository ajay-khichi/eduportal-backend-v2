package in.ignishers.eduportal.dto.attendance;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record StartAttendanceSessionRequest(
        @NotNull(message = "Session duration in minutes is required")
        @Min(value = 1, message = "Duration must be at least 1 minute")
        @Max(value = 180, message = "Duration cannot exceed 180 minutes")
        Integer durationMinutes,

        @DecimalMin(value = "-90.0", message = "Latitude must be >= -90")
        @DecimalMax(value = "90.0", message = "Latitude must be <= 90")
        BigDecimal latitude,

        @DecimalMin(value = "-180.0", message = "Longitude must be >= -180")
        @DecimalMax(value = "180.0", message = "Longitude must be <= 180")
        BigDecimal longitude,

        @Min(value = 10, message = "Geofence radius must be at least 10 meters")
        @Max(value = 2000, message = "Geofence radius cannot exceed 2000 meters")
        Integer radiusMeters
) {}
