package in.ignishers.eduportal.dto.attendance;

public record VerifyQrResponse(
        String challengeToken,
        int expiresInSeconds
) {}
