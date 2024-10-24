package com.project.unifiedauthenticationservice.Controllers;

import com.project.unifiedauthenticationservice.Controllers.Dto.AuthResponseDto;
import com.project.unifiedauthenticationservice.Controllers.Form.AuthenticationForm;
import com.project.unifiedauthenticationservice.Controllers.Form.RegistrationForm;
import com.project.unifiedauthenticationservice.config.JwtTokenUtil;
import com.project.unifiedauthenticationservice.converter.UserConverter;
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
@RequestMapping("/byUsernameAndPassword")
public class AuthorizationByUsernameAndPassword {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private UserService userService;

    @Autowired
    private UserConverter converter;

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> authenticateUser(@RequestBody AuthenticationForm form) {
        try {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        form.getUsername(),
                        form.getPassword()
                )
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = jwtTokenUtil.generateToken(form.getUsername());

        AuthResponseDto authResponse = new AuthResponseDto();
        authResponse.setToken(token);

        return ResponseEntity.ok(authResponse);
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new AuthResponseDto());
        }
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDto> registerUser(@RequestBody RegistrationForm form) {
        if (userService.findByUsername(form.getUsername()).isPresent())
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new AuthResponseDto());
        userService.createUser(form);
        return authenticateUser(converter.convertToAuthenticationForm(form));
    }
}
