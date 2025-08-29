package com.hmt.healix.repository;

import com.hmt.healix.entity.Wallet;
import com.hmt.healix.entity.WalletTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {
    List<WalletTransaction> findByWalletOrderByCreatedAtDesc(Wallet wallet);
}
