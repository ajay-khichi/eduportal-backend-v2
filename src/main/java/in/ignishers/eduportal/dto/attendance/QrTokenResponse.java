package in.ignishers.eduportal.dto.attendance;

public record QrTokenResponse(
        String token,
        int expiresInSeconds
) {}
