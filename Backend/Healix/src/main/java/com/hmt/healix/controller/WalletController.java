package com.hmt.healix.controller;

import com.hmt.healix.entity.Wallet;
import com.hmt.healix.entity.WalletTransaction;
import com.hmt.healix.service.WalletService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/patient/wallet")
public class WalletController {

    private final WalletService walletService;

    @GetMapping
    public ResponseEntity<Wallet> getWallet(HttpServletRequest req) {
        Wallet w = walletService.getMyWallet(req);
        return ResponseEntity.ok(w);
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<WalletTransaction>> transactions(HttpServletRequest req) {
        return ResponseEntity.ok(walletService.getMyTransactions(req));
    }

    @PostMapping("/topup/create-order")
    public ResponseEntity<?> createTopupOrder(@RequestParam int amount, HttpServletRequest req) throws Exception {
        return ResponseEntity.ok(walletService.createTopupOrder(req, amount));
    }
}
