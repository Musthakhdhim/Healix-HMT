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
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
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
        return walletRepo.findByPatient(p).orElseGet(() -> walletRepo.save(
                Wallet.builder().patient(p).balance(BigDecimal.ZERO).build()
        ));
    }

    public Wallet getMyWallet(HttpServletRequest authHeader) {
        String token = jwtService.getTokenFromAuthorization(authHeader);
        String email = jwtService.extractEmail(token);
        Patient p = patientRepo.findByUserEmail(email)
                .orElseThrow(() -> new UsersNotFoundException("Patient not found"));
        return getOrCreateWallet(p);
    }

    public List<WalletTransaction> getMyTransactions(HttpServletRequest authHeader) {
        Wallet w = getMyWallet(authHeader);
        return txnRepo.findByWalletOrderByCreatedAtDesc(w);
    }

    public Map<String, Object> createTopupOrder(HttpServletRequest authHeader, int amountInRupees) throws Exception {
        Wallet w = getMyWallet(authHeader);
        RazorpayClient client = new RazorpayClient(razorpayKeyId, razorpayKeySecret);

        JSONObject req = new JSONObject();
        req.put("amount", amountInRupees * 100);
        req.put("currency", "INR");
        req.put("receipt", "wallet_topup_" + w.getWalletId());
        Map<String, String> notes = new HashMap<>();
        notes.put("purpose", "WALLET_TOPUP");
        notes.put("patientId", String.valueOf(w.getPatient().getPatientId()));
        req.put("notes", notes);

        Order order = client.orders.create(req);

        Map<String, Object> res = new HashMap<>();
        res.put("id", order.get("id"));
        res.put("amount", order.get("amount"));
        res.put("currency", order.get("currency"));
        return res;
    }

    public void credit(Patient patient, BigDecimal amount, String reference, String description) {
        Wallet w = getOrCreateWallet(patient);
        w.setBalance(w.getBalance().add(amount));
        walletRepo.save(w);

        txnRepo.save(WalletTransaction.builder()
                .wallet(w).type(WalletTxnType.CREDIT)
                .amount(amount).reference(reference).description(description).build());
    }

    public void debit(Patient patient, BigDecimal amount, String reference, String description) {
        Wallet w = getOrCreateWallet(patient);
        if (w.getBalance().compareTo(amount) < 0)
            throw new IllegalStateException("Insufficient wallet balance");
        w.setBalance(w.getBalance().subtract(amount));
        walletRepo.save(w);

        txnRepo.save(WalletTransaction.builder()
                .wallet(w).type(WalletTxnType.DEBIT)
                .amount(amount).reference(reference).description(description).build());
    }
}
