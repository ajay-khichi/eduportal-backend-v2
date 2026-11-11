package in.ignishers.eduportal.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

public final class BiometricUtils {

    private BiometricUtils() {
    }

    public static byte[] toByteArray(List<Float> floats) {
        if (floats == null || floats.isEmpty()) {
            throw new IllegalArgumentException("Face embedding vector cannot be null or empty");
        }
        ByteBuffer buffer = ByteBuffer.allocate(floats.size() * Float.BYTES).order(ByteOrder.BIG_ENDIAN);
        for (Float value : floats) {
            if (value == null || value.isNaN() || value.isInfinite()) {
                throw new IllegalArgumentException("Face embedding contains invalid float values");
            }
            buffer.putFloat(value);
        }
        return buffer.array();
    }

    public static float[] toFloatArray(byte[] bytes) {
        if (bytes == null || bytes.length == 0 || bytes.length % Float.BYTES != 0) {
            throw new IllegalArgumentException("Invalid serialized face embedding byte array");
        }
        ByteBuffer buffer = ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN);
        float[] result = new float[bytes.length / Float.BYTES];
        for (int i = 0; i < result.length; i++) {
            result[i] = buffer.getFloat();
        }
        return result;
    }

    /**
     * Computes Cosine Similarity between two face embedding vectors.
     *
     * @return similarity score in [-1.0, 1.0]
     */
    public static double cosineSimilarity(float[] stored, List<Float> liveList) {
        if (stored == null || liveList == null || stored.length != liveList.size()) {
            throw new IllegalArgumentException(
                    "Face embedding dimension mismatch: expected "
                            + (stored != null ? stored.length : 0)
                            + " but got "
                            + (liveList != null ? liveList.size() : 0));
        }

        double dotProduct = 0.0;
        double normStored = 0.0;
        double normLive = 0.0;

        for (int i = 0; i < stored.length; i++) {
            Float liveValObj = liveList.get(i);
            if (liveValObj == null || liveValObj.isNaN() || liveValObj.isInfinite()) {
                throw new IllegalArgumentException("Live face embedding contains invalid float values");
            }
            double s = stored[i];
            double l = liveValObj;

            dotProduct += s * l;
            normStored += s * s;
            normLive += l * l;
        }

        double denominator = Math.sqrt(normStored) * Math.sqrt(normLive);
        if (denominator == 0.0) {
            return 0.0;
        }
        return dotProduct / denominator;
    }
}
