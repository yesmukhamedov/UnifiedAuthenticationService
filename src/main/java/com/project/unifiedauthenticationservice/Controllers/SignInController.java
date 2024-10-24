package com.project.unifiedauthenticationservice.Controllers;

import com.project.unifiedauthenticationservice.Controllers.Dto.AuthResponseDto;
import com.project.unifiedauthenticationservice.Controllers.Form.SignIn;
import com.project.unifiedauthenticationservice.config.JwtTokenUtil;
import com.project.unifiedauthenticationservice.models.User;
import com.project.unifiedauthenticationservice.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class SignInController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private UserService userService;

    @PostMapping("/signin")
    public ResponseEntity<AuthResponseDto> authenticateUser(@RequestBody SignIn signInRequest) {
        try {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        signInRequest.getUsername(),
                        signInRequest.getPassword()
                )
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = jwtTokenUtil.generateToken(signInRequest.getUsername());

        AuthResponseDto authResponse = new AuthResponseDto();
        authResponse.setToken(token);

        return ResponseEntity.ok(authResponse);
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new AuthResponseDto());
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<String> registerUser(@RequestBody User user) {
        if (userService.findByUsername(user.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Error: Username is already taken!");
        }
        userService.createUser(user);
        return ResponseEntity.ok("User registered successfully");
    }
}
