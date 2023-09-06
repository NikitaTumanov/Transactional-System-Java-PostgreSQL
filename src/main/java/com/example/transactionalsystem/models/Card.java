package com.example.transactionalsystem.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "card")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Card implements Comparable<Card> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "num", nullable = false, unique=true)
    int number;

    @Column(name = "balance", precision = 18, scale = 2, nullable = false)
    BigDecimal balance;

    @Column(name = "currency_code", length = 4, nullable = false)
    String currencyСode;

    @Column(name = "debit_frozen_balance", precision = 18, scale = 2)
    BigDecimal debitFrozenBalance;

    @Override
    public String toString() {
        return "{" +
                "Номер счета: " + number +
                ", Баланс: " + balance +
                ", Валюта: " + currencyСode +
                "}";
    }

    @Override
    public int compareTo(Card o) {
        return this.number - o.getNumber();
    }
}
