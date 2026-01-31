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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Authentication and session flows")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/start")
    @Operation(summary = "Start authentication challenge", tags = {"Authentication", "OTP"})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OTP challenge created"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "429", description = "Too many attempts")
    })
    public ResponseEntity<OtpChallengeResponse> start(@Valid @RequestBody AuthStartRequest request) {
        return ResponseEntity.ok(authService.start(request));
    }

    @PostMapping("/verify")
    @Operation(summary = "Verify authentication challenge", tags = {"Authentication", "OTP", "Tokens"})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Authentication verified"),
            @ApiResponse(responseCode = "400", description = "Invalid or expired code"),
            @ApiResponse(responseCode = "401", description = "Authentication failed")
    })
    public ResponseEntity<AuthVerifyResponse> verify(@Valid @RequestBody AuthVerifyRequest request) {
        return ResponseEntity.ok(authService.verify(request));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token", tags = {"Authentication", "Tokens"})
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token refreshed"),
            @ApiResponse(responseCode = "401", description = "Invalid refresh token")
    })
    public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return ResponseEntity.ok(authService.refresh(request.getRefreshToken()));
    }

    @PostMapping("/logout")
    @Operation(summary = "Invalidate refresh token", tags = {"Authentication", "Tokens"})
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Logged out"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    public ResponseEntity<Void> logout(@Valid @RequestBody LogoutRequest request) {
        authService.logout(request.getRefreshToken());
        return ResponseEntity.noContent().build();
    }
}
