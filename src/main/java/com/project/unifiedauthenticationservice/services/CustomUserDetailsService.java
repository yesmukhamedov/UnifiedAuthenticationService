package com.project.unifiedauthenticationservice.services;

import com.project.unifiedauthenticationservice.models.User;
import com.project.unifiedauthenticationservice.repositories.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
        true, //user.isEnabled(), // Проверка активности аккаунта
                true, // accountNonExpired
                true, // credentialsNonExpired
                true, //!user.isLocked(), // accountNonLocked
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))// user.getAuthorities() // Роли пользователя
        );
    }
}
