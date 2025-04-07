package com.github.oauth.repository;

import com.github.oauth.model.Role;
import com.github.oauth.model.AppRole;
import org.springframework.data.jpa.repository.JpaRepository;
// import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {

//    @Query("SELECT * FROM roles where roleName = ?1")
    Optional<Role> findByRoleName(AppRole name);
}
