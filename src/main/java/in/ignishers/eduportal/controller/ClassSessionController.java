package in.ignishers.eduportal.controller;

import in.ignishers.eduportal.dto.attendance.ClassSessionRequest;
import in.ignishers.eduportal.dto.attendance.ClassSessionResponse;
import in.ignishers.eduportal.dto.attendance.ClassStatusUpdateRequest;
import in.ignishers.eduportal.dto.attendance.RecurringScheduleRequest;
import in.ignishers.eduportal.service.ClassSessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class ClassSessionController {

    private final ClassSessionService classSessionService;

    @PostMapping("/class-sessions")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'FACULTY')")
    public ResponseEntity<ClassSessionResponse> scheduleClass(
            @Valid @RequestBody ClassSessionRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(classSessionService.scheduleClass(request));
    }

    @PostMapping("/class-sessions/recurring")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'FACULTY')")
    public ResponseEntity<List<ClassSessionResponse>> scheduleRecurringClasses(
            @Valid @RequestBody RecurringScheduleRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(classSessionService.scheduleRecurringClasses(request));
    }

    @GetMapping("/class-sessions/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'FACULTY', 'STUDENT')")
    public ResponseEntity<ClassSessionResponse> getClassSessionById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(classSessionService.getClassSessionById(id));
    }

    @GetMapping("/sections/{sectionId}/timetable")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'FACULTY', 'STUDENT')")
    public ResponseEntity<List<ClassSessionResponse>> getSectionTimetable(
            @PathVariable Long sectionId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ResponseEntity.ok(classSessionService.getSectionTimetable(sectionId, from, to));
    }

    @GetMapping("/faculty/{facultyId}/timetable")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'FACULTY')")
    public ResponseEntity<List<ClassSessionResponse>> getFacultyTimetable(
            @PathVariable Long facultyId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ResponseEntity.ok(classSessionService.getFacultyTimetable(facultyId, from, to));
    }

    @PutMapping("/class-sessions/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'FACULTY')")
    public ResponseEntity<ClassSessionResponse> updateClassSession(
            @PathVariable Long id,
            @Valid @RequestBody ClassSessionRequest request
    ) {
        return ResponseEntity.ok(classSessionService.updateClassSession(id, request));
    }

    @PatchMapping("/class-sessions/{id}/status")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'FACULTY')")
    public ResponseEntity<ClassSessionResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody ClassStatusUpdateRequest request
    ) {
        return ResponseEntity.ok(classSessionService.updateStatus(id, request));
    }
}
