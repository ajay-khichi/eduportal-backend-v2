package in.ignishers.eduportal.exception;

import in.ignishers.eduportal.exception.custom.EmailAlreadyExistsException;
import in.ignishers.eduportal.exception.custom.ResourceNotFoundException;
import in.ignishers.eduportal.exception.custom.TokenRefreshException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(
            MethodArgumentNotValidException exception
    ) {

        Map<String, String> errors = new LinkedHashMap<>();

        for (FieldError fieldError :
                exception.getBindingResult().getFieldErrors()) {

            errors.put(
                    fieldError.getField(),
                    fieldError.getDefaultMessage()
            );
        }

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(
                        ApiErrorResponse.validation(errors)
                );
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleBadCredentials(
            BadCredentialsException exception
    ) {

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(
                        new ApiErrorResponse(
                                401,
                                "Unauthorized",
                                "Invalid email or password"
                        )
                );
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ApiErrorResponse> handleDisabledUser(
            DisabledException exception
    ) {

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(
                        new ApiErrorResponse(
                                403,
                                "Forbidden",
                                "User account is disabled"
                        )
                );
    }

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ApiErrorResponse> handleLockedUser(
            LockedException exception
    ) {

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(
                        new ApiErrorResponse(
                                403,
                                "Forbidden",
                                "User account is locked"
                        )
                );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(
            Exception exception
    ) {

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(
                        new ApiErrorResponse(
                                500,
                                "Internal Server Error",
                                "An unexpected error occurred"
                        )
                );
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleEmailAlreadyExists(
            EmailAlreadyExistsException exception
    ) {

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(
                        new ApiErrorResponse(
                                409,
                                "Conflict",
                                exception.getMessage()
                        )
                );
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceNotFound(
            ResourceNotFoundException exception
    ) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(
                        new ApiErrorResponse(
                                404,
                                "Not Found",
                                exception.getMessage()
                        )
                );
    }

    @ExceptionHandler(TokenRefreshException.class)
    public ResponseEntity<ApiErrorResponse> handleTokenRefresh(
            TokenRefreshException exception
    ) {

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(
                        new ApiErrorResponse(
                                401,
                                "Unauthorized",
                                exception.getMessage()
                        )
                );
    }
}