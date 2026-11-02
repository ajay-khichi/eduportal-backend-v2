package in.ignishers.eduportal.controller;

import in.ignishers.eduportal.service.AuthService;
import in.ignishers.eduportal.dto.user.ChangePasswordRequest;
import in.ignishers.eduportal.dto.user.UserProfileResponse;
import in.ignishers.eduportal.security.UserPrincipal;
import in.ignishers.eduportal.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AuthService authService;

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getCurrentUser (
            @AuthenticationPrincipal UserPrincipal principal
            ) {
        UserProfileResponse response = userService.getCurrentUser(principal);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/sessions/revoke-all")
    public ResponseEntity<Void> revokeAllSessions (
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        authService.revokeAllUserTokens(principal.getId());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/change-password")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        userService.changePassword(principal.getId(), request);
        return ResponseEntity.noContent().build();
    }
}
