package in.ignishers.eduportal.controller;

import in.ignishers.eduportal.dto.curriculum.AcademicSessionRequest;
import in.ignishers.eduportal.dto.curriculum.AcademicSessionResponse;
import in.ignishers.eduportal.service.AcademicSessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class AcademicSessionController {

    private final AcademicSessionService sessionService;

    @PostMapping("/colleges/{collegeId}/academic-sessions")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public ResponseEntity<AcademicSessionResponse> createAcademicSession(
            @PathVariable Long collegeId,
            @Valid @RequestBody AcademicSessionRequest request
    ) {
        AcademicSessionResponse response = sessionService.createAcademicSession(collegeId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/colleges/{collegeId}/academic-sessions")
    public ResponseEntity<List<AcademicSessionResponse>> getAcademicSessionsByCollege(
            @PathVariable Long collegeId
    ) {
        return ResponseEntity.ok(sessionService.getAcademicSessionsByCollege(collegeId));
    }

    @GetMapping("/colleges/{collegeId}/academic-sessions/current")
    public ResponseEntity<AcademicSessionResponse> getCurrentAcademicSession(
            @PathVariable Long collegeId
    ) {
        return ResponseEntity.ok(sessionService.getCurrentAcademicSession(collegeId));
    }

    @GetMapping("/academic-sessions/{id}")
    public ResponseEntity<AcademicSessionResponse> getAcademicSessionById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(sessionService.getAcademicSessionById(id));
    }

    @PutMapping("/academic-sessions/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public ResponseEntity<AcademicSessionResponse> updateAcademicSession(
            @PathVariable Long id,
            @Valid @RequestBody AcademicSessionRequest request
    ) {
        return ResponseEntity.ok(sessionService.updateAcademicSession(id, request));
    }

    @PatchMapping("/colleges/{collegeId}/academic-sessions/{sessionId}/set-current")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public ResponseEntity<AcademicSessionResponse> setCurrentSession(
            @PathVariable Long collegeId,
            @PathVariable Long sessionId
    ) {
        return ResponseEntity.ok(sessionService.setCurrentSession(collegeId, sessionId));
    }

    @DeleteMapping("/academic-sessions/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public ResponseEntity<Void> deleteAcademicSession(
            @PathVariable Long id
    ) {
        sessionService.deleteAcademicSession(id);
        return ResponseEntity.noContent().build();
    }
}