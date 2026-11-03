package in.ignishers.eduportal.dto.institutional;

import java.time.LocalDateTime;

public record DepartmentResponse(
        Long id,
        Long collegeId,
        String CollegeName,
        String name,
        String code,
        Long hodFacultyId,
        String hodName,
        String hodEmail,
        LocalDateTime createdAt
) {
}
