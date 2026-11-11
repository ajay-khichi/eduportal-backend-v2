package in.ignishers.eduportal.controller;

import in.ignishers.eduportal.dto.people.TeachingAssignmentRequest;
import in.ignishers.eduportal.dto.people.TeachingAssignmentResponse;
import in.ignishers.eduportal.service.TeachingAssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class TeachingAssignmentController {

    private final TeachingAssignmentService assignmentService;

    @PostMapping("/teaching-assignments")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'FACULTY')")
    public ResponseEntity<TeachingAssignmentResponse> createAssignment(
            @Valid @RequestBody TeachingAssignmentRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(assignmentService.createAssignment(request));
    }

    @GetMapping("/faculty/{facultyId}/teaching-assignments")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'FACULTY')")
    public ResponseEntity<List<TeachingAssignmentResponse>> getAssignmentsByFaculty(
            @PathVariable Long facultyId
    ) {
        return ResponseEntity.ok(assignmentService.getAssignmentsByFaculty(facultyId));
    }

    @GetMapping("/faculty/{facultyId}/teaching-assignments/session/{sessionId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'FACULTY')")
    public ResponseEntity<List<TeachingAssignmentResponse>> getAssignmentsByFacultyAndSession(
            @PathVariable Long facultyId,
            @PathVariable Long sessionId
    ) {
        return ResponseEntity.ok(assignmentService.getAssignmentsByFacultyAndSession(facultyId, sessionId));
    }

    @GetMapping("/sections/{sectionId}/teaching-assignments")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'FACULTY')")
    public ResponseEntity<List<TeachingAssignmentResponse>> getAssignmentsBySection(
            @PathVariable Long sectionId
    ) {
        return ResponseEntity.ok(assignmentService.getAssignmentsBySection(sectionId));
    }

    @DeleteMapping("/teaching-assignments/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public ResponseEntity<Void> deleteAssignment(
            @PathVariable Long id
    ) {
        assignmentService.deleteAssignment(id);
        return ResponseEntity.noContent().build();
    }
}