package com.demo.jwt.demoJWT.validation;

import com.demo.jwt.demoJWT.services.UserService;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ExistsByUsernameValidation implements ConstraintValidator<ExistsByUsername, String> {

    @Autowired
    UserService userService;

    @Override
    public boolean isValid(String username, ConstraintValidatorContext constraintValidatorContext) {
        if(userService != null){ //Por alguna razon cuando se ejecuta la anotacion se hace dos veces el proceso, y en la segunda vez, el servicio no se inyecta
            return !userService.existsByUsername(username);
        }

        return true;
    }
}
