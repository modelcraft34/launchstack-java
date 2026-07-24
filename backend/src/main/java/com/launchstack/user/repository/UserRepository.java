package com.launchstack.user.repository;

import com.launchstack.user.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    long countByRoles_Name(String roleName);

    long countByRoles_NameAndEnabledTrueAndAccountNonLockedTrue(String roleName);
}
