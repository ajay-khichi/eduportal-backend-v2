package in.ignishers.eduportal.controller;

import in.ignishers.eduportal.dto.institutional.AssignHodRequest;
import in.ignishers.eduportal.dto.institutional.DepartmentRequest;
import in.ignishers.eduportal.dto.institutional.DepartmentResponse;
import in.ignishers.eduportal.service.DepartmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService deptService;

    @PostMapping("/colleges/{collegeId}/departments")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public ResponseEntity<DepartmentResponse> createDepartment(
            @PathVariable Long collegeId,
            @Valid @RequestBody DepartmentRequest request
            ) {
        DepartmentResponse response = deptService.createDepartment(collegeId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/colleges/{collegeId}/departments")
    public ResponseEntity<List<DepartmentResponse>> getDepartmentsByCollege(
            @PathVariable Long collegeId
    ) {
        List<DepartmentResponse> responses = deptService.getDepartmentsByCollege(collegeId);
        return ResponseEntity.status(HttpStatus.OK).body(responses);
    }

    @GetMapping("/departments/{id}")
    public ResponseEntity<DepartmentResponse> getDepartmentById(
            @PathVariable Long id
    ) {
        DepartmentResponse response = deptService.getDepartmentById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/departments/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public ResponseEntity<DepartmentResponse> updateDepartment(
            @PathVariable Long id,
            @Valid @RequestBody DepartmentRequest request
    ) {
        DepartmentResponse response = deptService.updateDepartment(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/departments/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public ResponseEntity<Void> deleteDepartment(
            @PathVariable Long id
    ) {
        deptService.deleteDepartment(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/departments/{id}/assign-hod")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public ResponseEntity<DepartmentResponse> assignHod(
            @PathVariable Long id,
            @Valid @RequestBody AssignHodRequest request
    ) {
        DepartmentResponse response = deptService.assignHod(id, request);
        return ResponseEntity.ok(response);
    }
}
