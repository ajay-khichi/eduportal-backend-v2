package in.ignishers.eduportal.controller;

import in.ignishers.eduportal.dto.curriculum.SubjectRequest;
import in.ignishers.eduportal.dto.curriculum.SubjectResponse;
import in.ignishers.eduportal.service.SubjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class SubjectController {

    private final SubjectService subjectService;

    @PostMapping("/department-programs/{departmentProgramId}/subjects")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'HOD')")
    public ResponseEntity<SubjectResponse> createSubject(
            @PathVariable Long departmentProgramId,
            @Valid @RequestBody SubjectRequest request
    ) {
        SubjectResponse response = subjectService.createSubject(departmentProgramId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/department-programs/{departmentProgramId}/subjects")
    public ResponseEntity<List<SubjectResponse>> getSubjectsByDepartmentProgram(
            @PathVariable Long departmentProgramId,
            @RequestParam(required = false) Integer semester
    ) {
        List<SubjectResponse> list = subjectService.getSubjectsByDepartmentProgram(departmentProgramId, semester);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/subjects/{id}")
    public ResponseEntity<SubjectResponse> getSubjectById(
            @PathVariable Long id
    ) {
        SubjectResponse response = subjectService.getSubjectById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/subjects/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'HOD')")
    public ResponseEntity<SubjectResponse> updateSubject(
            @PathVariable Long id,
            @Valid @RequestBody SubjectRequest request
    ) {
        SubjectResponse response = subjectService.updateSubject(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/subjects/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'HOD')")
    public ResponseEntity<Void> deleteSubject(
            @PathVariable Long id
    ) {
        subjectService.deleteSubject(id);
        return ResponseEntity.noContent().build();
    }
}