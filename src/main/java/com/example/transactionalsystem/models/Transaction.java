package com.example.transactionalsystem.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "transaction")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "from_card_id")
    int fromCardId;

    @Column(name = "to_card_id")
    int toCardId;

    @Column(name = "amount", precision = 18, scale = 2, nullable = false)
    BigDecimal amount;

    @Column(name = "currency_code", length = 3, nullable = false)
    String currencyСode;

    @Column(name = "status", length = 7, nullable = false)
    String status;
}
