package com.demo.jwt.demoJWT.repositories;

import com.demo.jwt.demoJWT.entities.User;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserRepository extends CrudRepository<User, Long> {
    //CrudRepository es más básico, pensado para operaciones CRUD (Create, Read, Update, Delete) y métodos comunes: save, findById, findAll, delete, etc.
    //JpaRepository extiende de CrudRepository idea para trabajar con JPA y se dispone de funcionaes mas avanzadas
    boolean existsByUsername(String username);
    Optional<User> findByUsername(String username);
}