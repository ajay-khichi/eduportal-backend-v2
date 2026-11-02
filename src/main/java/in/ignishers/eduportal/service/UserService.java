package in.ignishers.eduportal.service;

import in.ignishers.eduportal.dto.user.ChangePasswordRequest;
import in.ignishers.eduportal.dto.user.UserProfileResponse;
import in.ignishers.eduportal.exception.custom.ResourceNotFoundException;
import in.ignishers.eduportal.models.auth.User;
import in.ignishers.eduportal.repos.auth.UserRepository;
import in.ignishers.eduportal.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;

    public UserProfileResponse getCurrentUser(UserPrincipal principal) {
        User user = userRepo.findById(principal.getId())
                .orElseThrow( () ->
                        new ResourceNotFoundException("User not found")
                );

        return new UserProfileResponse(
                user.getEmail(),
                user.getRole(),
                user.isActive(),
                user.getCreatedAt()
        );
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepo.findById(userId)
                .orElseThrow( () ->
                    new ResourceNotFoundException("User not found")
                );

        if(!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Current password is incorrect");
        }

        if(passwordEncoder.matches(request.newPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("New password cannot be the same as the current password");
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepo.save(user);

        authService.revokeAllUserTokens(userId);
    }
}
