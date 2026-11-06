package in.ignishers.eduportal.dto.curriculum;

import java.time.LocalDateTime;

public record SectionResponse(
        Long id,
        Long departmentProgramId,
        String departmentName,
        String programName,
        String programCode,
        Long academicSessionId,
        String academicSessionName,
        Integer semester,
        String name,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}