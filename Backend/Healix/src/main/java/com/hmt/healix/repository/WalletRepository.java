package com.hmt.healix.repository;

import com.hmt.healix.entity.Wallet;
import com.hmt.healix.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, Long> {
    Optional<Wallet> findByPatient(Patient patient);
}
