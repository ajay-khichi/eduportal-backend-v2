package in.ignishers.eduportal.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.HexFormat;

public final class QrTokenUtils {

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int SIGNATURE_BYTES = 12; // 96-bit MAC = 16 Base64URL chars
    public static final int DEFAULT_STEP_SECONDS = 5;

    private QrTokenUtils() {
    }

    public record ChallengePayload(
            Long attendanceId,
            Long studentId,
            Long sessionId,
            long expiresAtEpochSec,
            String signature
    ) {}

    /**
     * Generates a cryptographically strong 256-bit (64 hex chars) session secret.
     */
    public static String generateSecret() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

    /**
     * Generates a compact rolling time-stepped QR token for an attendance session.
     * Format: <sessionId>.<timeStep>.<16-char-hmacSignature>
     */
    public static String generateRollingToken(Long sessionId, String secret, int stepSeconds) {
        long timeStep = Instant.now().getEpochSecond() / stepSeconds;
        String payload = sessionId + ":" + timeStep;
        String signature = computeCompactHmac(payload, secret);
        return sessionId + "." + timeStep + "." + signature;
    }

    /**
     * Seconds remaining in the current QR time window (1..stepSeconds).
     */
    public static int secondsRemaining(int stepSeconds) {
        long now = Instant.now().getEpochSecond();
        return (int) (stepSeconds - (now % stepSeconds));
    }

    /**
     * Extracts the AttendanceSession ID from a QR token without verifying signature yet.
     */
    public static Long extractSessionId(String qrToken) {
        if (qrToken == null || qrToken.isBlank()) {
            throw new IllegalArgumentException("QR token cannot be empty");
        }
        String[] parts = qrToken.trim().split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid QR token format");
        }
        try {
            return Long.parseLong(parts[0]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid session ID in QR token");
        }
    }

    /**
     * Verifies a scanned QR token against the session secret and current 5-second window.
     * Allows current window and 1 previous window (grace period for scanning right on rollover).
     */
    public static boolean verifyRollingToken(Long expectedSessionId, String secret, String qrToken, int stepSeconds) {
        if (qrToken == null || qrToken.isBlank() || secret == null) {
            return false;
        }

        String[] parts = qrToken.trim().split("\\.");
        if (parts.length != 3) {
            return false;
        }

        long tokenSessionId;
        long tokenStep;
        try {
            tokenSessionId = Long.parseLong(parts[0]);
            tokenStep = Long.parseLong(parts[1]);
        } catch (NumberFormatException e) {
            return false;
        }

        if (!expectedSessionId.equals(tokenSessionId)) {
            return false;
        }

        long currentStep = Instant.now().getEpochSecond() / stepSeconds;
        if (tokenStep < currentStep - 1 || tokenStep > currentStep) {
            return false;
        }

        String payload = tokenSessionId + ":" + tokenStep;
        String expectedSignature = computeCompactHmac(payload, secret);

        return MessageDigest.isEqual(
                expectedSignature.getBytes(StandardCharsets.UTF_8),
                parts[2].getBytes(StandardCharsets.UTF_8)
        );
    }

    /**
     * Issues a short-lived, student-bound challenge token after QR verification succeeds.
     * Format: <attendanceId>.<studentId>.<sessionId>.<expiresAtEpochSec>.<hmacSignature>
     */
    public static String generateChallengeToken(
            Long attendanceId,
            Long studentId,
            Long sessionId,
            int ttlSeconds,
            String secret
    ) {
        long expiresAt = Instant.now().getEpochSecond() + ttlSeconds;
        String payload = "CHAL:" + attendanceId + ":" + studentId + ":" + sessionId + ":" + expiresAt;
        String signature = computeCompactHmac(payload, secret);
        return attendanceId + "." + studentId + "." + sessionId + "." + expiresAt + "." + signature;
    }

    /**
     * Parses a challenge token so the caller can fetch the session secret for signature verification.
     */
    public static ChallengePayload parseChallengeToken(String challengeToken) {
        if (challengeToken == null || challengeToken.isBlank()) {
            throw new IllegalArgumentException("Challenge token cannot be empty");
        }
        String[] parts = challengeToken.trim().split("\\.");
        if (parts.length != 5) {
            throw new IllegalArgumentException("Invalid challenge token format");
        }
        try {
            return new ChallengePayload(
                    Long.parseLong(parts[0]),
                    Long.parseLong(parts[1]),
                    Long.parseLong(parts[2]),
                    Long.parseLong(parts[3]),
                    parts[4]
            );
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Malformed challenge token values");
        }
    }

    /**
     * Verifies challenge signature, student binding, and 20-second expiry window.
     */
    public static boolean verifyChallengeToken(ChallengePayload challenge, Long expectedStudentId, String secret) {
        if (challenge == null || secret == null || !challenge.studentId().equals(expectedStudentId)) {
            return false;
        }
        if (Instant.now().getEpochSecond() > challenge.expiresAtEpochSec()) {
            return false;
        }
        String payload = "CHAL:" + challenge.attendanceId() + ":" + challenge.studentId()
                + ":" + challenge.sessionId() + ":" + challenge.expiresAtEpochSec();
        String expectedSignature = computeCompactHmac(payload, secret);

        return MessageDigest.isEqual(
                expectedSignature.getBytes(StandardCharsets.UTF_8),
                challenge.signature().getBytes(StandardCharsets.UTF_8)
        );
    }

    private static String computeCompactHmac(String payload, String secret) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM);
            mac.init(keySpec);
            byte[] fullHmac = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            byte[] truncated = Arrays.copyOf(fullHmac, SIGNATURE_BYTES);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(truncated);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("Failed to compute HMAC signature for QR token", e);
        }
    }
}
