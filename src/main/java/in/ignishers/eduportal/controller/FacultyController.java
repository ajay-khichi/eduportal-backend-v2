package in.ignishers.eduportal.controller;

import in.ignishers.eduportal.dto.people.FacultyRequest;
import in.ignishers.eduportal.dto.people.FacultyResponse;
import in.ignishers.eduportal.service.FacultyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class FacultyController {

    private final FacultyService facultyService;

    @PostMapping("/colleges/{collegeId}/faculty")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public ResponseEntity<FacultyResponse> createFaculty(
            @PathVariable Long collegeId,
            @Valid @RequestBody FacultyRequest request
    ) {
        FacultyResponse response = facultyService.createFaculty(collegeId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/colleges/{collegeId}/faculty")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'FACULTY')")
    public ResponseEntity<List<FacultyResponse>> getFacultyByCollege(
            @PathVariable Long collegeId
    ) {
        return ResponseEntity.ok(facultyService.getFacultyByCollege(collegeId));
    }

    @GetMapping("/departments/{departmentId}/faculty")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'FACULTY')")
    public ResponseEntity<List<FacultyResponse>> getFacultyByDepartment(
            @PathVariable Long departmentId
    ) {
        return ResponseEntity.ok(facultyService.getFacultyByDepartment(departmentId));
    }

    @GetMapping("/faculty/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'FACULTY')")
    public ResponseEntity<FacultyResponse> getFacultyById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(facultyService.getFacultyById(id));
    }

    @PutMapping("/faculty/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public ResponseEntity<FacultyResponse> updateFaculty(
            @PathVariable Long id,
            @Valid @RequestBody FacultyRequest request
    ) {
        return ResponseEntity.ok(facultyService.updateFaculty(id, request));
    }

    @DeleteMapping("/faculty/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public ResponseEntity<Void> deleteFaculty(
            @PathVariable Long id
    ) {
        facultyService.deleteFaculty(id);
        return ResponseEntity.noContent().build();
    }
}