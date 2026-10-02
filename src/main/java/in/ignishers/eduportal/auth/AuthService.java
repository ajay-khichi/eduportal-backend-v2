package in.ignishers.eduportal.auth;

import in.ignishers.eduportal.auth.dto.AuthResponse;
import in.ignishers.eduportal.auth.dto.LoginRequest;
import in.ignishers.eduportal.auth.dto.RefreshTokenRequest;
import in.ignishers.eduportal.auth.dto.RegisterRequest;
import in.ignishers.eduportal.enums.UserRole;
import in.ignishers.eduportal.exception.custom.EmailAlreadyExistsException;
import in.ignishers.eduportal.exception.custom.TokenRefreshException;
import in.ignishers.eduportal.models.RefreshToken;
import in.ignishers.eduportal.models.User;
import in.ignishers.eduportal.repos.RefreshTokenRepository;
import in.ignishers.eduportal.repos.UserRepository;
import in.ignishers.eduportal.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public AuthResponse register(RegisterRequest request) {

        String email = normalizeEmail(request.email());

        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException(
                    "An account with this email already exists"
            );
        }

        User user = new User();

        user.setEmail(email);
        user.setPasswordHash(
                passwordEncoder.encode(request.password())
        );
        user.setRole(UserRole.STUDENT);
        user.setActive(true);

        user = userRepository.save(user);

        return createAuthResponse(user);
    }

    public AuthResponse login(LoginRequest request) {

        String email = normalizeEmail(request.email());

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        email,
                        request.password()
                )
        );

        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Authenticated user was not found"
                        )
                );

        return createAuthResponse(user);
    }

    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {

        String tokenHash = hashToken(
                request.refreshToken()
        );

        RefreshToken existingToken =
                refreshTokenRepository
                        .findByTokenHash(tokenHash)
                        .orElseThrow(() ->
                                new TokenRefreshException(
                                        "Invalid refresh token"
                                )
                        );

        if (existingToken.isRevoked()) {
            throw new TokenRefreshException(
                    "Refresh token has been revoked"
            );
        }

        if (existingToken.getExpiresAt().isBefore(Instant.now())) {
            existingToken.setRevoked(true);
            refreshTokenRepository.save(existingToken);

            throw new TokenRefreshException(
                    "Refresh token has expired"
            );
        }

        User user = existingToken.getUser();

        if (!user.isActive()) {
            throw new TokenRefreshException(
                    "User account is disabled"
            );
        }

        // Rotate refresh token
        existingToken.setRevoked(true);
        refreshTokenRepository.save(existingToken);

        return createAuthResponse(user);
    }

    @Transactional
    public void logout(RefreshTokenRequest request) {

        String tokenHash = hashToken(
                request.refreshToken()
        );

        refreshTokenRepository
                .findByTokenHash(tokenHash)
                .ifPresent(token -> {
                    token.setRevoked(true);
                    refreshTokenRepository.save(token);
                });
    }

    private AuthResponse createAuthResponse(User user) {

        String accessToken =
                jwtService.generateAccessToken(user);

        String refreshToken =
                generateRefreshToken();

        saveRefreshToken(
                user,
                refreshToken
        );

        long expiresIn =
                jwtService.getAccessTokenExpirationSeconds();

        return new AuthResponse(
                accessToken,
                refreshToken,
                "Bearer",
                expiresIn
        );
    }

    private void saveRefreshToken(
            User user,
            String rawToken
    ) {

        RefreshToken refreshToken =
                new RefreshToken();

        refreshToken.setUser(user);
        refreshToken.setTokenHash(
                hashToken(rawToken)
        );
        refreshToken.setExpiresAt(
                Instant.now().plusMillis(
                        jwtService.getRefreshTokenExpiration()
                )
        );
        refreshToken.setRevoked(false);

        refreshTokenRepository.save(refreshToken);
    }

    private String generateRefreshToken() {

        byte[] randomBytes = new byte[64];

        secureRandom.nextBytes(randomBytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);
    }

    private String hashToken(String token) {

        try {

            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash =
                    digest.digest(
                            token.getBytes(StandardCharsets.UTF_8)
                    );

            StringBuilder hexString =
                    new StringBuilder(hash.length * 2);

            for (byte b : hash) {
                hexString.append(
                        String.format("%02x", b)
                );
            }

            return hexString.toString();

        } catch (NoSuchAlgorithmException exception) {

            throw new IllegalStateException(
                    "SHA-256 algorithm is not available",
                    exception
            );
        }
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }
}