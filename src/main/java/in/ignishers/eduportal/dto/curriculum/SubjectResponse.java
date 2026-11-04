package in.ignishers.eduportal.dto.curriculum;

import java.time.LocalDateTime;

public record SubjectResponse(
        Long id,
        Long departmentProgramId,
        String programCode,
        String departmentCode,
        String name,
        String code,
        Integer semester,
        Integer credits,
        LocalDateTime createdAt
) {
}
