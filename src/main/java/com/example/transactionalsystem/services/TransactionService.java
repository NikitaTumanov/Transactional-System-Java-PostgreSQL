package com.example.transactionalsystem.services;

import com.example.transactionalsystem.models.Card;
import com.example.transactionalsystem.models.Transaction;
import com.example.transactionalsystem.repositories.CardRepository;
import com.example.transactionalsystem.repositories.TransactionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class TransactionService {
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private CardRepository cardRepository;

    public TransactionService(CardRepository cardRepository, TransactionRepository transactionRepository){
        this.cardRepository=cardRepository;
        this.transactionRepository=transactionRepository;
    }

    public void create(BigDecimal amount, int senderCardNumber, int recipientCardNumber) {
        Transaction transaction = new Transaction();
        transaction.setFromCardId(senderCardNumber);
        transaction.setToCardId(recipientCardNumber);
        transaction.setAmount(amount);
        transaction.setStatus("Created");
        transactionRepository.save(transaction);
    }
}
