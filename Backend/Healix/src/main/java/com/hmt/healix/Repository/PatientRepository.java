package com.hmt.healix.Repository;

import com.hmt.healix.Entity.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Integer> {
    Optional<Patient> findByUser_UserId(Long userId);

    Page<Patient> findAllByUserEnabledTrue(Pageable pageable);

    Page<Patient> findAllByUserUsernameContainingIgnoreCaseOrUserEmailContainingIgnoreCase(String username, String email, Pageable pageable);


}
