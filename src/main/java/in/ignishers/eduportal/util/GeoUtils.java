package in.ignishers.eduportal.util;

import java.math.BigDecimal;

public final class GeoUtils {

    private static final double EARTH_RADIUS_METERS = 6_371_000.0;

    private GeoUtils() {
    }

    /**
     * Calculates the great-circle distance between two GPS coordinates using the Haversine formula.
     *
     * @return distance in meters
     */
    public static double calculateDistanceMeters(
            BigDecimal lat1, BigDecimal lon1,
            BigDecimal lat2, BigDecimal lon2
    ) {
        if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) {
            throw new IllegalArgumentException("GPS coordinates cannot be null");
        }

        double phi1 = Math.toRadians(lat1.doubleValue());
        double phi2 = Math.toRadians(lat2.doubleValue());
        double deltaPhi = Math.toRadians(lat2.doubleValue() - lat1.doubleValue());
        double deltaLambda = Math.toRadians(lon2.doubleValue() - lon1.doubleValue());

        double a = Math.sin(deltaPhi / 2.0) * Math.sin(deltaPhi / 2.0)
                + Math.cos(phi1) * Math.cos(phi2)
                * Math.sin(deltaLambda / 2.0) * Math.sin(deltaLambda / 2.0);

        double c = 2.0 * Math.atan2(Math.sqrt(a), Math.sqrt(1.0 - a));

        return EARTH_RADIUS_METERS * c;
    }

    public static boolean isWithinRadius(
            BigDecimal centerLat, BigDecimal centerLon,
            BigDecimal targetLat, BigDecimal targetLon,
            double maxRadiusMeters
    ) {
        return calculateDistanceMeters(centerLat, centerLon, targetLat, targetLon) <= maxRadiusMeters;
    }
}
