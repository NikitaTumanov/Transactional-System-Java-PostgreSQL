package com.example.transactionalsystem.repositories;

import com.example.transactionalsystem.models.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface CardRepository extends JpaRepository<Card, String> {
    Card findById(int id);
    Card findByNumber(int number);
    Card findByNumberAndCurrencyСode(int number, String currencyСode);
    List<Card> findAllByDebitFrozenBalance(BigDecimal debitFrozenBalance);
}
