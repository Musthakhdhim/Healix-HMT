package com.hmt.healix.repository;

import com.hmt.healix.entity.Doctor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DoctorRepository extends JpaRepository<Doctor, Integer> {
    Optional<Doctor> findByUser_UserId(Long userId);

    Page<Doctor> findAllByUserAdminAuthorisedTrueAndUserEnabledTrue(Pageable pageable);

    Page<Doctor> findAllByUserUsernameContainingIgnoreCaseOrUserEmailContainingIgnoreCase(String username,String email, Pageable pageable);

    Page<Doctor> findAllBySpecializationContainingIgnoreCaseOrUserUsernameContainingIgnoreCase(String specialization,String username, Pageable pageable);

    Optional<Doctor> findByUserEmail(String email);
}
