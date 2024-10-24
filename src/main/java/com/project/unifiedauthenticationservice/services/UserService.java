package com.project.unifiedauthenticationservice.services;

import com.project.unifiedauthenticationservice.Controllers.Form.RegistrationForm;
import com.project.unifiedauthenticationservice.converter.UserConverter;
import com.project.unifiedauthenticationservice.models.User;
import com.project.unifiedauthenticationservice.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserConverter converter;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, UserConverter converter, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.converter = converter;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User createUser(RegistrationForm form) {
        User user = converter.convertToModel(form);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public User updateUser(Long id, RegistrationForm form) {
        User user = userRepository.findById(id).orElse(null);
        if (user != null)
            return userRepository.save(converter.convertToModel(form));
        return null;
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}
