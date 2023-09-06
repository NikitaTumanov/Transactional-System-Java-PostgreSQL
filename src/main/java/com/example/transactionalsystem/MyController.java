package com.example.transactionalsystem;

import com.example.transactionalsystem.models.Card;
import com.example.transactionalsystem.services.CardService;
import com.example.transactionalsystem.services.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@Controller
public class MyController {
    @Autowired
    private CardService cardService;
    @Autowired
    private TransactionService transactionService;

    String errorMessage = "Что-то пошло не так. ";
    int systemCardNumber = 0;

    @PostMapping("/newCard")
    ResponseEntity<String> createCard(Integer cardNumber, BigDecimal balance, String currencyCode){
        try {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(cardService.create(cardNumber, balance, currencyCode));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.OK).body(errorMessage
                    + "Возможно, карта с таким номером уже есть.");
        }

    }

    @PostMapping("/invoice")
    ResponseEntity<String> invoice(String currencyСode, BigDecimal amount, Integer senderCardNumber){
        int recipientCardNumber = systemCardNumber;
        try {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(cardService.changeBalance(currencyСode, amount, senderCardNumber, recipientCardNumber));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.OK).body(errorMessage);
        }
    }

    @PostMapping("/withdraw")
    ResponseEntity<String> withdraw(String currencyСode, BigDecimal amount, Integer recipientCardNumber){
        int senderCardNumber = systemCardNumber;
        try {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(cardService.changeBalance(currencyСode, amount, senderCardNumber, recipientCardNumber));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.OK).body(errorMessage);
        }
    }

    @GetMapping("/show")
    ResponseEntity<String> showTransactions(){
        try {
            List<Card> activeCards = cardService.findActiveCards();
            List<Card> allCards = cardService.findAllCards();
            Collections.sort(allCards);
            String message = allCards.toString();

            allCards.removeAll(activeCards);
            allCards.forEach((a)->a.setBalance(
                    a.getBalance().add(a.getDebitFrozenBalance())));

            return ResponseEntity.status(HttpStatus.OK)
                    .body("Актуальные балансы:\n" + message + "\nЗамороженные балансы:\n" + allCards);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.OK).body(errorMessage);
        }
    }
}
