package com.demo.jwt.demoJWT.repositories;

import com.demo.jwt.demoJWT.entities.Role;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

public interface RoleRepository extends CrudRepository<Role,Long> {
    //Gracias a CrudRepository automaticamente ese metodo realiza una consulta a BD por el nombre del role
    Optional<Role> findByName(String name);
}
