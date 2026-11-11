package in.ignishers.eduportal.dto.attendance;

import in.ignishers.eduportal.enums.ClassStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record ClassSessionResponse(
        Long id,
        Long teachingAssignmentId,
        Long facultyId,
        String facultyName,
        Long subjectId,
        String subjectName,
        String subjectCode,
        Long sectionId,
        String sectionName,
        Integer semester,
        LocalDate classDate,
        LocalTime startTime,
        LocalTime endTime,
        String room,
        ClassStatus status,
        LocalDateTime createdAt
) {}
