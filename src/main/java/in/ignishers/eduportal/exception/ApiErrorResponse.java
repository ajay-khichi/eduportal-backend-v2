package in.ignishers.eduportal.exception;

import java.time.Instant;
import java.util.Map;

public record ApiErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        Map<String, String> validationErrors
) {

    public ApiErrorResponse(
            int status,
            String error,
            String message
    ) {
        this(
                Instant.now(),
                status,
                error,
                message,
                null
        );
    }

    public static ApiErrorResponse validation(
            Map<String, String> errors
    ) {
        return new ApiErrorResponse(
                Instant.now(),
                400,
                "Bad Request",
                "Validation failed",
                errors
        );
    }
}