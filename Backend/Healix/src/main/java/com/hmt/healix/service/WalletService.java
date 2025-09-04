package com.hmt.healix.service;

import com.hmt.healix.entity.*;
import com.hmt.healix.exception.UsersNotFoundException;
import com.hmt.healix.repository.PatientRepository;
import com.hmt.healix.repository.WalletRepository;
import com.hmt.healix.repository.WalletTransactionRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletService {

    private final WalletRepository walletRepo;
    private final WalletTransactionRepository txnRepo;
    private final PatientRepository patientRepo;
    private final JwtService jwtService;

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;
    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    public Wallet getOrCreateWallet(Patient p) {
        log.info("Fetching wallet for patientId: {}", p.getPatientId());
        return walletRepo.findByPatient(p).orElseGet(() -> {
            log.info("No wallet found for patientId: {}. Creating new wallet.", p.getPatientId());
            Wallet wallet = Wallet.builder().patient(p).balance(BigDecimal.ZERO).build();
            return walletRepo.save(wallet);
        });
    }

    public Wallet getMyWallet(HttpServletRequest authHeader) {
        log.info("Fetching wallet for logged-in user");
        String token = jwtService.getTokenFromAuthorization(authHeader);
        String email = jwtService.extractEmail(token);
        log.debug("Extracted email from token: {}", email);

        Patient p = patientRepo.findByUserEmail(email)
                .orElseThrow(() -> {
                    log.error("Patient not found for email: {}", email);
                    return new UsersNotFoundException("Patient not found");
                });

        Wallet wallet = getOrCreateWallet(p);
        log.info("Fetched wallet successfully for patientId: {}", p.getPatientId());
        return wallet;
    }

    public List<WalletTransaction> getMyTransactions(HttpServletRequest authHeader) {
        log.info("Fetching wallet transactions for logged-in user");
        Wallet w = getMyWallet(authHeader);
        List<WalletTransaction> transactions = txnRepo.findByWalletOrderByCreatedAtDesc(w);
        log.info("Fetched {} transactions for walletId: {}", transactions.size(), w.getWalletId());
        return transactions;
    }

    public Map<String, Object> createTopupOrder(HttpServletRequest authHeader, int amountInRupees) throws Exception {
        log.info("Creating wallet top-up order for amount: {} INR", amountInRupees);
        Wallet w = getMyWallet(authHeader);

        RazorpayClient client = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
        log.debug("Initialized Razorpay client for walletId: {}", w.getWalletId());

        JSONObject req = new JSONObject();
        req.put("amount", amountInRupees * 100);
        req.put("currency", "INR");
        req.put("receipt", "wallet_topup_" + w.getWalletId());

        Map<String, String> notes = new HashMap<>();
        notes.put("purpose", "WALLET_TOPUP");
        notes.put("patientId", String.valueOf(w.getPatient().getPatientId()));
        req.put("notes", notes);

        log.debug("Razorpay top-up request: {}", req);

        Order order = client.orders.create(req);
        log.info("Created Razorpay order successfully. OrderId: {}", Optional.ofNullable(order.get("id")));

        Map<String, Object> res = new HashMap<>();
        res.put("id", order.get("id"));
        res.put("amount", order.get("amount"));
        res.put("currency", order.get("currency"));

        log.info("Returning top-up order response for walletId: {}", w.getWalletId());
        return res;
    }

    public void credit(Patient patient, BigDecimal amount, String reference, String description) {
        log.info("Crediting wallet. PatientId: {}, Amount: {}, Reference: {}", patient.getPatientId(), amount, reference);
        Wallet w = getOrCreateWallet(patient);
        w.setBalance(w.getBalance().add(amount));
        walletRepo.save(w);

        txnRepo.save(WalletTransaction.builder()
                .wallet(w).type(WalletTxnType.CREDIT)
                .amount(amount).reference(reference).description(description).build());

        log.info("Wallet credited successfully. New Balance: {}", w.getBalance());
    }

    public void debit(Patient patient, BigDecimal amount, String reference, String description) {
        log.info("Debiting wallet. PatientId: {}, Amount: {}, Reference: {}", patient.getPatientId(), amount, reference);
        Wallet w = getOrCreateWallet(patient);

        if (w.getBalance().compareTo(amount) < 0) {
            log.error("Insufficient balance for walletId: {}. Requested: {}, Available: {}",
                    w.getWalletId(), amount, w.getBalance());
            throw new IllegalStateException("Insufficient wallet balance");
        }

        w.setBalance(w.getBalance().subtract(amount));
        walletRepo.save(w);

        txnRepo.save(WalletTransaction.builder()
                .wallet(w).type(WalletTxnType.DEBIT)
                .amount(amount).reference(reference).description(description).build());

        log.info("Wallet debited successfully. New Balance: {}", w.getBalance());
    }
}
