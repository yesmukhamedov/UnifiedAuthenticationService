package men.yeskendyr.auth.controller;

import jakarta.validation.Valid;
import java.util.UUID;
import men.yeskendyr.auth.dto.IdentifierResponse;
import men.yeskendyr.auth.dto.IdentifierStartRequest;
import men.yeskendyr.auth.dto.IdentifierVerifyRequest;
import men.yeskendyr.auth.dto.MeResponse;
import men.yeskendyr.auth.dto.OtpChallengeResponse;
import men.yeskendyr.auth.entity.IdentifierType;
import men.yeskendyr.auth.security.UserPrincipal;
import men.yeskendyr.auth.service.IdentifierNormalizer;
import men.yeskendyr.auth.service.MeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/me")
public class MeController {
    private final MeService meService;

    public MeController(MeService meService) {
        this.meService = meService;
    }

    @GetMapping
    public ResponseEntity<MeResponse> me(@AuthenticationPrincipal UserPrincipal principal) {
        UUID userId = principal.userId();
        return ResponseEntity.ok(new MeResponse(userId, meService.listIdentifiers(userId)));
    }

    @PostMapping("/identifiers/start")
    public ResponseEntity<OtpChallengeResponse> startLink(@AuthenticationPrincipal UserPrincipal principal,
                                                          @Valid @RequestBody IdentifierStartRequest request) {
        IdentifierType type = request.email() != null ? IdentifierType.EMAIL : IdentifierType.PHONE;
        String value = IdentifierNormalizer.normalize(type,
                request.email() != null ? request.email() : request.phoneNumber());
        OtpChallengeResponse response = meService.startLink(principal.userId(), type, value);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/identifiers/verify")
    public ResponseEntity<IdentifierResponse> verifyLink(@AuthenticationPrincipal UserPrincipal principal,
                                                         @Valid @RequestBody IdentifierVerifyRequest request) {
        IdentifierResponse response = meService.verifyLink(principal.userId(), request.challengeId(), request.code());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
