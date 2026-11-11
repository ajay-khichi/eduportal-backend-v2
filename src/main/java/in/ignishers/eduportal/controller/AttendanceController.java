package in.ignishers.eduportal.controller;

import in.ignishers.eduportal.dto.attendance.*;
import in.ignishers.eduportal.security.UserPrincipal;
import in.ignishers.eduportal.service.AttendanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping("/class-sessions/{classId}/attendance-session")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'FACULTY')")
    public ResponseEntity<AttendanceSessionResponse> startSession(
            @PathVariable Long classId,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody StartAttendanceSessionRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(attendanceService.startSession(classId, principal.getId(), isAdmin(principal), request));
    }

    @GetMapping("/attendance-sessions/{sessionId}/qr-token")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'FACULTY')")
    public ResponseEntity<QrTokenResponse> getRollingQrToken(
            @PathVariable Long sessionId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(
                attendanceService.getRollingQrToken(sessionId, principal.getId(), isAdmin(principal)));
    }

    @PatchMapping("/attendance-sessions/{sessionId}/stop")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'FACULTY')")
    public ResponseEntity<AttendanceSessionResponse> stopSession(
            @PathVariable Long sessionId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(
                attendanceService.stopSession(sessionId, principal.getId(), isAdmin(principal)));
    }

    @PostMapping("/attendance/mark")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<MarkAttendanceResponse> markAttendance(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody MarkAttendanceRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(attendanceService.markAttendance(principal.getId(), request));
    }

    @PostMapping("/class-sessions/{classId}/attendance/manual")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'FACULTY')")
    public ResponseEntity<AttendanceRecordResponse> markManualAttendance(
            @PathVariable Long classId,
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ManualAttendanceRequest request
    ) {
        return ResponseEntity.ok(
                attendanceService.markManualAttendance(classId, principal.getId(), isAdmin(principal), request));
    }

    @GetMapping("/class-sessions/{classId}/attendance")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'FACULTY')")
    public ResponseEntity<List<AttendanceRecordResponse>> getClassAttendance(
            @PathVariable Long classId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(
                attendanceService.getClassAttendance(classId, principal.getId(), isAdmin(principal)));
    }

    @GetMapping("/students/me/attendance-summary")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<SubjectAttendanceSummaryResponse>> getMyAttendanceSummary(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam Long academicSessionId
    ) {
        return ResponseEntity.ok(
                attendanceService.getMyAttendanceSummary(principal.getId(), academicSessionId));
    }

    @GetMapping("/students/{studentId}/attendance-summary")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'FACULTY')")
    public ResponseEntity<List<SubjectAttendanceSummaryResponse>> getStudentAttendanceSummary(
            @PathVariable Long studentId,
            @RequestParam Long academicSessionId
    ) {
        return ResponseEntity.ok(
                attendanceService.getStudentAttendanceSummary(studentId, academicSessionId));
    }

    private boolean isAdmin(UserPrincipal principal) {
        return principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN")
                        || a.getAuthority().equals("ROLE_COLLEGE_ADMIN"));
    }
}
