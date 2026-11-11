package in.ignishers.eduportal.dto.attendance;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record FaceProfileRequest(
        @NotEmpty(message = "Face embedding vector is required")
        @Size(min = 64, max = 2048, message = "Face embedding dimension must be between 64 and 2048")
        List<Float> embedding,

        @NotBlank(message = "Model version is required (e.g. mobilefacenet-v1)")
        @Size(max = 50, message = "Model version cannot exceed 50 characters")
        String modelVersion
) {}
