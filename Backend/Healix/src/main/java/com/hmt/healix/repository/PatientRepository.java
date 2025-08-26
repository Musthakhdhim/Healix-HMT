package com.hmt.healix.repository;

import com.hmt.healix.entity.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Integer> {
    Optional<Patient> findByUser_UserId(Long userId);

    Page<Patient> findAllByUserEnabledTrue(Pageable pageable);

    Page<Patient> findAllByUserUsernameContainingIgnoreCaseOrUserEmailContainingIgnoreCase(String username, String email, Pageable pageable);


    Optional<Patient> findByUserEmail(String email);
}
