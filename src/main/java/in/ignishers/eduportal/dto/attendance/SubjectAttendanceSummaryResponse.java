package in.ignishers.eduportal.dto.attendance;

public record SubjectAttendanceSummaryResponse(
        Long subjectId,
        String subjectName,
        String subjectCode,
        long totalClasses,
        long attendedClasses,
        double attendancePercentage
) {}
