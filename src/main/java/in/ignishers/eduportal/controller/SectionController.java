package in.ignishers.eduportal.controller;

import in.ignishers.eduportal.dto.curriculum.SectionRequest;
import in.ignishers.eduportal.dto.curriculum.SectionResponse;
import in.ignishers.eduportal.service.SectionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class SectionController {

    private final SectionService sectionService;

    @PostMapping("/sections")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'FACULTY')")
    public ResponseEntity<SectionResponse> createSection(
            @Valid @RequestBody SectionRequest request
    ) {
        SectionResponse response = sectionService.createSection(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/sections/{id}")
    public ResponseEntity<SectionResponse> getSectionById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(sectionService.getSectionById(id));
    }

    @GetMapping("/academic-sessions/{sessionId}/sections")
    public ResponseEntity<List<SectionResponse>> getSectionsBySession(
            @PathVariable Long sessionId
    ) {
        return ResponseEntity.ok(sectionService.getSectionsBySession(sessionId));
    }

    @GetMapping("/sections")
    public ResponseEntity<List<SectionResponse>> getSections(
            @RequestParam Long departmentProgramId,
            @RequestParam Long academicSessionId,
            @RequestParam Integer semester
    ) {
        return ResponseEntity.ok(sectionService.getSections(departmentProgramId, academicSessionId, semester));
    }

    @PutMapping("/sections/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'FACULTY')")
    public ResponseEntity<SectionResponse> updateSection(
            @PathVariable Long id,
            @Valid @RequestBody SectionRequest request
    ) {
        return ResponseEntity.ok(sectionService.updateSection(id, request));
    }

    @DeleteMapping("/sections/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public ResponseEntity<Void> deleteSection(
            @PathVariable Long id
    ) {
        sectionService.deleteSection(id);
        return ResponseEntity.noContent().build();
    }
}