package com.hmt.healix.repository;

import com.hmt.healix.entity.Role;
import com.hmt.healix.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<Users, Long> {
    Optional<Users> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    List<Users> findByRoleAndAdminAuthorised(Role role, boolean isAdminAuthorised);


}
