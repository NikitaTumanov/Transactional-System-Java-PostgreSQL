package com.example.transactionalsystem.services;

import com.example.transactionalsystem.models.Card;
import com.example.transactionalsystem.repositories.CardRepository;
import com.example.transactionalsystem.repositories.TransactionRepository;
import com.github.sarxos.xchange.ExchangeCache;
import com.github.sarxos.xchange.ExchangeException;
import com.github.sarxos.xchange.ExchangeRate;
import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.StoredProcedureQuery;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class CardService {
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private CardRepository cardRepository;
    @Autowired
    private TransactionService transactionService;
    @PersistenceContext
    private EntityManager entityManager;

    public CardService(CardRepository cardRepository, TransactionRepository transactionRepository){
        this.cardRepository=cardRepository;
        this.transactionRepository=transactionRepository;
    }

    public String create(int cardNumber, BigDecimal balance, String currencyCode) {
        Card card = new Card();
        card.setNumber(cardNumber);
        card.setBalance(balance);
        card.setCurrencyСode(currencyCode);
        card.setDebitFrozenBalance(BigDecimal.valueOf(0));
        cardRepository.save(card);
        return "Карта: " + cardRepository.findByNumber(cardNumber) + " создана";
    }

    public List<Card> findAllCards(){
        return cardRepository.findAll();
    }

    public List<Card> findActiveCards(){
        return cardRepository.findAllByDebitFrozenBalance(BigDecimal.valueOf(0));
    }

    @SneakyThrows
    public BigDecimal convert(String currencyСode, BigDecimal amount, int cardNumber){
        ExchangeCache.setParameter("openexchangerates.org.apikey", "aef7a51a486c47a09be01ffb1cfaa005");
        ExchangeCache cache = new ExchangeCache(cardRepository.findByNumber(cardNumber).getCurrencyСode());
        // currencyСode переводим в cardHolderCurrencyСode
        ExchangeRate rate = cache.getRate(currencyСode);
        return rate.convert(amount);
    }

    public int createTransaction(String currencyСode, BigDecimal amount, Integer senderCardNumber, Integer recipientCardNumber){
        Object transactionNumber = entityManager
                .createNativeQuery("select create_transaction(:amount, :sender_card_number, :recipient_card_number, :currency_code)")
                .setParameter("amount", amount)
                .setParameter("sender_card_number", senderCardNumber)
                .setParameter("recipient_card_number", recipientCardNumber)
                .setParameter("currency_code", currencyСode)
                        .getSingleResult();
        System.out.println((int) transactionNumber);
        return (int) transactionNumber;
    }

    public boolean freezeSenderBalance(Integer transactionId, BigDecimal senderAmount, Integer senderCardNumber){
        Object transactionStatus = entityManager
                .createNativeQuery("select freeze_sender_balance(:id_transaction, :sender_amount, :sender_card_number)")
                .setParameter("id_transaction", transactionId)
                .setParameter("sender_amount", senderAmount)
                .setParameter("sender_card_number", senderCardNumber)
                .getSingleResult();
        System.out.println((boolean) transactionStatus);
        return (boolean) transactionStatus;
    }

    public void increaseBalance(Integer transactionId, BigDecimal senderAmount, BigDecimal recipientAmount, Integer senderCardNumber, Integer recipientCardNumber){
        entityManager
                .createNativeQuery("select increase_balance(:id_transaction, :sender_amount, :recipient_amount, :sender_card_number, :recipient_card_number)")
                .setParameter("id_transaction", transactionId)
                .setParameter("sender_amount", senderAmount)
                .setParameter("recipient_amount", recipientAmount)
                .setParameter("sender_card_number", senderCardNumber)
                .setParameter("recipient_card_number", recipientCardNumber)
                .getSingleResult();
    }

    public String changeBalance(String currencyСode, BigDecimal amount, Integer senderCardNumber, Integer recipientCardNumber){
        String senderBalanceBefore = cardRepository.findByNumber(senderCardNumber).getBalance().toString();
        String recipientBalanceBefore = cardRepository.findByNumber(recipientCardNumber).getBalance().toString();

        BigDecimal senderAmount = convert(currencyСode, amount, senderCardNumber);
        BigDecimal recipientAmount = convert(currencyСode, amount, recipientCardNumber);
        System.out.println(senderAmount + "\n"+ recipientAmount);

        int transactionId = createTransaction(currencyСode, amount, senderCardNumber, recipientCardNumber);
        boolean transactionStatus = freezeSenderBalance(transactionId, senderAmount, senderCardNumber);
        if (transactionStatus){
            increaseBalance(transactionId, senderAmount, recipientAmount, senderCardNumber, recipientCardNumber);
        }
        else {
            return "У отправителя недостаточно средств";
        }

        if (senderCardNumber == 0){
            return "С баланса системы переведено " + amount + " " + currencyСode + " на счет " + recipientCardNumber;
        } else {
            return "Баланс системы пополнен на " + amount + " " + currencyСode + " со счета " + senderCardNumber;
        }
    }
}
