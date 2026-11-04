package in.ignishers.eduportal.dto.curriculum;

import java.time.LocalDateTime;

public record DepartmentProgramResponse(
        Long id,
        Long departmentId,
        String departmentName,
        String departmentCode,
        Long programId,
        String programName,
        String programCode,
        LocalDateTime createdAt
) {
}
