package com.hmt.healix.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletTransaction {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne @JoinColumn(name="wallet_id", nullable = false)
    private Wallet wallet;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private WalletTxnType type;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(length = 64)
    private String reference;

    @Column(length = 255)
    private String description;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() { this.createdAt = LocalDateTime.now(); }
}
