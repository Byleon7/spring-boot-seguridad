package com.demo.jwt.demoJWT.controllers;

import com.demo.jwt.demoJWT.entities.User;
import com.demo.jwt.demoJWT.services.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Usuarios", description = "Operaciones relacionadas con Usuarios")
public class UserController {

    @Autowired
    UserService userService;

    @GetMapping
    @Operation(summary = "Obtener todos los usuarios")
    public List<User> list(){
        return userService.findAll();
    }

    @PostMapping
    @Operation(summary = "Creacion de usuarios")
    public ResponseEntity<?> create(@Valid @RequestBody User user, BindingResult result){
        if(result.hasErrors()){
            return validation(result);
        }

        User userCreated = userService.save(user);
        return new ResponseEntity<>(userCreated, HttpStatus.CREATED);
    }
    //Servicio publico para registro de usuarios (No hace falta estar logueado)
    @PostMapping("/register")
    @Operation(summary = "Registro de Usuarios")
    public ResponseEntity<?> register(@Valid @RequestBody User user, BindingResult result){
        user.setAdmin(false);
        return create(user, result);
    }

    private ResponseEntity<?> validation(BindingResult result){
        Map<String,String> errors = new HashMap<>();

        result.getFieldErrors().forEach(error -> {
                errors.put(error.getField(), error.getDefaultMessage());
            }
        );

        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }
}
