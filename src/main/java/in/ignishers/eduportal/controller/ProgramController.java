package in.ignishers.eduportal.controller;

import in.ignishers.eduportal.dto.curriculum.DepartmentProgramResponse;
import in.ignishers.eduportal.dto.curriculum.ProgramRequest;
import in.ignishers.eduportal.dto.curriculum.ProgramResponse;
import in.ignishers.eduportal.service.ProgramService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ProgramController {

    private final ProgramService programService;
    // ==========================================
    // PROGRAM CRUD ENDPOINTS
    // ==========================================
    @PostMapping("/programs")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public ResponseEntity<ProgramResponse> createProgram(
            @Valid @RequestBody ProgramRequest request
    ) {
        ProgramResponse response = programService.createProgram(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    @GetMapping("/programs")
    public ResponseEntity<List<ProgramResponse>> getAllPrograms() {
        List<ProgramResponse> list = programService.getAllPrograms();
        return ResponseEntity.ok(list);
    }
    @GetMapping("/programs/{id}")
    public ResponseEntity<ProgramResponse> getProgramById(
            @PathVariable Long id
    ) {
        ProgramResponse response = programService.getProgramById(id);
        return ResponseEntity.ok(response);
    }
    @PutMapping("/programs/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public ResponseEntity<ProgramResponse> updateProgram(
            @PathVariable Long id,
            @Valid @RequestBody ProgramRequest request
    ) {
        ProgramResponse response = programService.updateProgram(id, request);
        return ResponseEntity.ok(response);
    }
    @DeleteMapping("/programs/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public ResponseEntity<Void> deleteProgram(
            @PathVariable Long id
    ) {
        programService.deleteProgram(id);
        return ResponseEntity.noContent().build();
    }

    // ==========================================
    // DEPARTMENT - PROGRAM ASSOCIATION ENDPOINTS
    // ==========================================
    @PostMapping("/departments/{departmentId}/programs/{programId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'HOD')")
    public ResponseEntity<DepartmentProgramResponse> linkProgramToDepartment(
            @PathVariable Long departmentId,
            @PathVariable Long programId
    ) {
        DepartmentProgramResponse response = programService.linkProgramToDepartment(departmentId, programId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    @GetMapping("/departments/{departmentId}/programs")
    public ResponseEntity<List<DepartmentProgramResponse>> getProgramsByDepartment(
            @PathVariable Long departmentId
    ) {
        List<DepartmentProgramResponse> list = programService.getProgramsByDepartment(departmentId);
        return ResponseEntity.ok(list);
    }
    @DeleteMapping("/department-programs/{departmentProgramId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public ResponseEntity<Void> unlinkProgramFromDepartment(
            @PathVariable Long departmentProgramId
    ) {
        programService.unlinkProgramFromDepartment(departmentProgramId);
        return ResponseEntity.noContent().build();
    }
}
