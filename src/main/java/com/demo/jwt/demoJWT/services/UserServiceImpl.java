package com.demo.jwt.demoJWT.services;

import com.demo.jwt.demoJWT.entities.Role;
import com.demo.jwt.demoJWT.entities.User;
import com.demo.jwt.demoJWT.repositories.RoleRepository;
import com.demo.jwt.demoJWT.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository repository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public List<User> findAll() {
        return (List<User>) repository.findAll();
    }

    @Override
    @Transactional
    public User save(User user) {
        //Se agregan los roles al usuario teniendo en cuenta si es admin o no y ademas de encripta password
        Optional<Role> optionalRole = roleRepository.findByName("ROLE_USER");
        List<Role> roles = new ArrayList<>();
        optionalRole.ifPresent(roles::add); //Si optionalRole no esta vacio, agrega lo encontrado a la lista 'roles'
        if(user.isAdmin()){
            optionalRole = roleRepository.findByName("ROLE_ADMIN");
            optionalRole.ifPresent(roles::add);
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRoles(roles);
        return repository.save(user);
    }

    @Override
    public boolean existsByUsername(String username) {
        return repository.existsByUsername(username);
    }

    //Por defecto al usar Spring Security el servicio de autenticacion es /login por defecto, no hace falta definirlo en un controlador
}
