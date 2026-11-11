package in.ignishers.eduportal.dto.attendance;

import jakarta.validation.constraints.NotBlank;

public record VerifyQrRequest(
        @NotBlank(message = "QR token is required")
        String qrToken
) {}
