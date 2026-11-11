package in.ignishers.eduportal.util;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SmartAttendanceUtilsTest {

    @Test
    void qrRollingToken_shouldVerifyValidTokenAndRejectTamperedOrExpired() {
        String secret = QrTokenUtils.generateSecret();
        assertEquals(64, secret.length());

        String token = QrTokenUtils.generateRollingToken(10L, secret, 5);
        assertTrue(QrTokenUtils.verifyRollingToken(10L, secret, token, 5));

        // Wrong session ID
        assertFalse(QrTokenUtils.verifyRollingToken(99L, secret, token, 5));

        // Tampered signature
        String tampered = token.substring(0, token.length() - 2) + "XX";
        assertFalse(QrTokenUtils.verifyRollingToken(10L, secret, tampered, 5));

        // Old time step (expired screenshot from 1 minute ago)
        long expiredStep = (Instant.now().getEpochSecond() - 60) / 5;
        String[] parts = token.split("\\.");
        String oldStepToken = parts[0] + "." + expiredStep + "." + parts[2];
        assertFalse(QrTokenUtils.verifyRollingToken(10L, secret, oldStepToken, 5));
    }

    @Test
    void challengeToken_shouldBindStudentAndEnforceExpiry() {
        String secret = QrTokenUtils.generateSecret();
        String challenge = QrTokenUtils.generateChallengeToken(45L, 102L, 10L, 20, secret);

        QrTokenUtils.ChallengePayload parsed = QrTokenUtils.parseChallengeToken(challenge);
        assertEquals(45L, parsed.attendanceId());
        assertEquals(102L, parsed.studentId());
        assertEquals(10L, parsed.sessionId());

        assertTrue(QrTokenUtils.verifyChallengeToken(parsed, 102L, secret));
        // Different student attempting to reuse friend's challenge
        assertFalse(QrTokenUtils.verifyChallengeToken(parsed, 999L, secret));

        // Expired challenge
        String expiredChallenge = QrTokenUtils.generateChallengeToken(45L, 102L, 10L, -5, secret);
        QrTokenUtils.ChallengePayload parsedExpired = QrTokenUtils.parseChallengeToken(expiredChallenge);
        assertFalse(QrTokenUtils.verifyChallengeToken(parsedExpired, 102L, secret));
    }

    @Test
    void geoUtils_shouldComputeHaversineDistanceAccurately() {
        BigDecimal lat1 = new BigDecimal("28.6139000");
        BigDecimal lon1 = new BigDecimal("77.2090000");

        assertEquals(0.0, GeoUtils.calculateDistanceMeters(lat1, lon1, lat1, lon1), 0.01);

        // ~0.0005 deg latitude is ~55.6 meters
        BigDecimal nearbyLat = new BigDecimal("28.6144000");
        double nearbyDist = GeoUtils.calculateDistanceMeters(lat1, lon1, nearbyLat, lon1);
        assertTrue(nearbyDist > 50.0 && nearbyDist < 62.0);
        assertTrue(GeoUtils.isWithinRadius(lat1, lon1, nearbyLat, lon1, 100.0));
        assertFalse(GeoUtils.isWithinRadius(lat1, lon1, nearbyLat, lon1, 30.0));
    }

    @Test
    void biometricUtils_shouldRoundTripAndComputeCosineSimilarity() {
        List<Float> v1 = List.of(1.0f, 0.0f, 0.5f, -0.5f);
        byte[] serialized = BiometricUtils.toByteArray(v1);
        assertEquals(16, serialized.length);

        float[] restored = BiometricUtils.toFloatArray(serialized);
        assertEquals(1.0f, restored[0]);
        assertEquals(-0.5f, restored[3]);

        assertEquals(1.0, BiometricUtils.cosineSimilarity(restored, v1), 1e-6);

        float[] ortho = new float[]{1.0f, 0.0f};
        assertEquals(0.0, BiometricUtils.cosineSimilarity(ortho, List.of(0.0f, 1.0f)), 1e-6);

        assertThrows(IllegalArgumentException.class,
                () -> BiometricUtils.cosineSimilarity(ortho, List.of(1.0f, 2.0f, 3.0f)));
    }
}
