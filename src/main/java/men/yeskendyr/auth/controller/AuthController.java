package men.yeskendyr.auth.controller;

import jakarta.validation.Valid;
import men.yeskendyr.auth.dto.AuthStartRequest;
import men.yeskendyr.auth.dto.AuthVerifyRequest;
import men.yeskendyr.auth.dto.AuthVerifyResponse;
import men.yeskendyr.auth.dto.LogoutRequest;
import men.yeskendyr.auth.dto.OtpChallengeResponse;
import men.yeskendyr.auth.dto.RefreshRequest;
import men.yeskendyr.auth.dto.TokenResponse;
import men.yeskendyr.auth.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/start")
    public ResponseEntity<OtpChallengeResponse> start(@Valid @RequestBody AuthStartRequest request) {
        return ResponseEntity.ok(authService.start(request));
    }

    @PostMapping("/verify")
    public ResponseEntity<AuthVerifyResponse> verify(@Valid @RequestBody AuthVerifyRequest request) {
        return ResponseEntity.ok(authService.verify(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return ResponseEntity.ok(authService.refresh(request.getRefreshToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody LogoutRequest request) {
        authService.logout(request.getRefreshToken());
        return ResponseEntity.noContent().build();
    }
}
