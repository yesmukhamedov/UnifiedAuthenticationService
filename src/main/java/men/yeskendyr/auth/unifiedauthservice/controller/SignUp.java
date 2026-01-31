package men.yeskendyr.auth.unifiedauthservice.controller;

import men.yeskendyr.auth.dto.RegisterRequest;
import men.yeskendyr.auth.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public class SignUp {
    @Autowired
    AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.ok("User registered");
    }
}
