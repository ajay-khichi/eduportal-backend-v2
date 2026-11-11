package in.ignishers.eduportal.dto.people;

import jakarta.validation.constraints.NotNull;

public record TeachingAssignmentRequest(
        @NotNull(message = "Faculty ID is required")
        Long facultyId,

        @NotNull(message = "Subject ID is required")
        Long subjectId,

        @NotNull(message = "Section ID is required")
        Long sectionId,

        @NotNull(message = "Academic Session ID is required")
        Long academicSessionId
) {}