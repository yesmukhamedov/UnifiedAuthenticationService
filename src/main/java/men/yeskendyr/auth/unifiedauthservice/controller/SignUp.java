package men.yeskendyr.auth.unifiedauthservice.controller;

public class SignUp {
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.ok("User registered");
    }
}
