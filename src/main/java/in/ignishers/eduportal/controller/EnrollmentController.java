package in.ignishers.eduportal.controller;

import in.ignishers.eduportal.dto.people.BulkEnrollmentRequest;
import in.ignishers.eduportal.dto.people.EnrollmentRequest;
import in.ignishers.eduportal.dto.people.EnrollmentResponse;
import in.ignishers.eduportal.service.EnrollmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @PostMapping("/sections/{sectionId}/enrollments")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'FACULTY')")
    public ResponseEntity<EnrollmentResponse> enrollStudent(
            @PathVariable Long sectionId,
            @Valid @RequestBody EnrollmentRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(enrollmentService.enrollStudent(sectionId, request));
    }

    @PostMapping("/sections/{sectionId}/enrollments/bulk")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'FACULTY')")
    public ResponseEntity<List<EnrollmentResponse>> enrollBulk(
            @PathVariable Long sectionId,
            @Valid @RequestBody BulkEnrollmentRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(enrollmentService.enrollBulk(sectionId, request));
    }

    @GetMapping("/sections/{sectionId}/enrollments")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'FACULTY')")
    public ResponseEntity<List<EnrollmentResponse>> getEnrollmentsBySection(
            @PathVariable Long sectionId
    ) {
        return ResponseEntity.ok(enrollmentService.getEnrollmentsBySection(sectionId));
    }

    @GetMapping("/students/{studentId}/enrollments")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'FACULTY', 'STUDENT')")
    public ResponseEntity<List<EnrollmentResponse>> getEnrollmentsByStudent(
            @PathVariable Long studentId
    ) {
        return ResponseEntity.ok(enrollmentService.getEnrollmentsByStudent(studentId));
    }

    @DeleteMapping("/sections/{sectionId}/enrollments/{studentId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'FACULTY')")
    public ResponseEntity<Void> unenrollStudent(
            @PathVariable Long sectionId,
            @PathVariable Long studentId
    ) {
        enrollmentService.unenrollStudent(sectionId, studentId);
        return ResponseEntity.noContent().build();
    }
}