package in.ignishers.eduportal.dto.attendance;

import in.ignishers.eduportal.enums.ClassStatus;
import jakarta.validation.constraints.NotNull;

public record ClassStatusUpdateRequest(
        @NotNull(message = "Class status is required")
        ClassStatus status
) {}
