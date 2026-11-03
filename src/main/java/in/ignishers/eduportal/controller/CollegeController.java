package in.ignishers.eduportal.controller;

import in.ignishers.eduportal.dto.institutional.AssignCollegeAdminRequest;
import in.ignishers.eduportal.dto.institutional.CollegeRequest;
import in.ignishers.eduportal.dto.institutional.CollegeResponse;
import in.ignishers.eduportal.service.CollegeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/colleges")
@RequiredArgsConstructor
public class CollegeController {

    private final CollegeService collegeService;

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<CollegeResponse> createCollege(
            @Valid @RequestBody CollegeRequest request
            ) {
        CollegeResponse response = collegeService.createCollege(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<CollegeResponse>> getAllColleges() {
        List<CollegeResponse> colleges = collegeService.getAllColleges();
        return ResponseEntity.ok(colleges);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CollegeResponse> getCollege(
            @PathVariable Long id
    ) {
        CollegeResponse college = collegeService.getCollegeById(id);
        return ResponseEntity.ok(college);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<CollegeResponse> updateCollege(
            @PathVariable Long id,
            @Valid @RequestBody CollegeRequest request
    ) {
        CollegeResponse college = collegeService.updateCollege(id, request);
        return ResponseEntity.ok(college);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> deleteCollege(
            @PathVariable Long id
    ) {
        collegeService.deleteCollege(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/assign-admin")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<CollegeResponse>  assignCollegeAdmin(
            @PathVariable Long id,
            @Valid @RequestBody AssignCollegeAdminRequest request
    ) {
        CollegeResponse response = collegeService.assignCollegeAdmin(id, request);
        return ResponseEntity.ok(response);
    }
}
