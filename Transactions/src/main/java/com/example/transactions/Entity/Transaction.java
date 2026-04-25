package com.example.transactions.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Data
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fromIban;

    @Column(nullable = false)
    private String toIban;

    @Column(nullable = false)
    private Instant time;

    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    @Enumerated(EnumType.STRING)
    private TransactionType type;

    @Column(nullable = false)
    private BigDecimal amount;

    private String description;
    @PrePersist
    public void onCreate(){
        time = Instant.now();
    }
}
