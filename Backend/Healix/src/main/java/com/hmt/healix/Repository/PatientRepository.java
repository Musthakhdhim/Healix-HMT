package com.hmt.healix.Repository;

import com.hmt.healix.Entity.Patient;
import com.hmt.healix.Entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Integer> {
    Optional<Patient> findByUser_UserId(Long userId);
}
