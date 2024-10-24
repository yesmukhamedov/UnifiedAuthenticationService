package com.project.unifiedauthenticationservice.converter;

import com.project.unifiedauthenticationservice.Controllers.Form.AuthenticationForm;
import com.project.unifiedauthenticationservice.Controllers.Form.RegistrationForm;
import com.project.unifiedauthenticationservice.models.User;
import org.springframework.stereotype.Component;

@Component
public class UserConverter {

    public User convertToModel(RegistrationForm form) {
        User model = new User();

        model.setUsername(form.getUsername());
        model.setPassword(form.getPassword());
        return model;
    }

    public AuthenticationForm convertToAuthenticationForm(RegistrationForm form) {
        AuthenticationForm authenticationForm = new AuthenticationForm();

        authenticationForm.setUsername(form.getUsername());
        authenticationForm.setPassword(form.getPassword());
        return authenticationForm;
    }
}
