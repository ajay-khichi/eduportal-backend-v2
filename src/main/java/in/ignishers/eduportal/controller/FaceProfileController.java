package in.ignishers.eduportal.controller;

import in.ignishers.eduportal.dto.attendance.FaceProfileRequest;
import in.ignishers.eduportal.dto.attendance.FaceProfileResponse;
import in.ignishers.eduportal.security.UserPrincipal;
import in.ignishers.eduportal.service.FaceProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class FaceProfileController {

    private final FaceProfileService faceProfileService;

    @PostMapping("/students/me/face-profile")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<FaceProfileResponse> enrollMyFaceProfile(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody FaceProfileRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(faceProfileService.enrollMyFaceProfile(principal.getId(), request));
    }

    @GetMapping("/students/me/face-profile")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<FaceProfileResponse> getMyFaceProfile(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(faceProfileService.getMyFaceProfile(principal.getId()));
    }

    @PostMapping("/students/{studentId}/face-profile")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'FACULTY')")
    public ResponseEntity<FaceProfileResponse> enrollStudentFaceProfile(
            @PathVariable Long studentId,
            @Valid @RequestBody FaceProfileRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(faceProfileService.enrollOrUpdateFaceProfile(studentId, request));
    }

    @GetMapping("/students/{studentId}/face-profile")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'FACULTY')")
    public ResponseEntity<FaceProfileResponse> getStudentFaceProfile(
            @PathVariable Long studentId
    ) {
        return ResponseEntity.ok(faceProfileService.getFaceProfileByStudentId(studentId));
    }

    @DeleteMapping("/students/{studentId}/face-profile")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public ResponseEntity<Void> deactivateStudentFaceProfile(
            @PathVariable Long studentId
    ) {
        faceProfileService.deactivateFaceProfile(studentId);
        return ResponseEntity.noContent().build();
    }
}
