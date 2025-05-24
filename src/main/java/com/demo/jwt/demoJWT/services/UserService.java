package com.demo.jwt.demoJWT.services;

import com.demo.jwt.demoJWT.entities.User;

import java.util.List;

public interface UserService {

    List<User> findAll();
    User save(User user);
    boolean existsByUsername(String username);
}
