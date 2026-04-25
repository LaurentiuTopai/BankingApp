package com.example.accounts.Service;

import com.example.accounts.Entity.Account;
import com.example.accounts.Entity.AccountDTO;
import com.example.accounts.Repository.AccountRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class AccountService {
    private final AccountRepository myAccount;
    private final KafkaTemplate<String,String> kafkaTemplate;
    @Autowired
    public AccountService(AccountRepository myAccount,
                          KafkaTemplate<String,String> kafkaTemplate){
        this.myAccount = myAccount;
        this.kafkaTemplate = kafkaTemplate;
    }
    @Transactional
    public AccountDTO createAccount(Account newAccount){
        if(!myAccount.existsAccountsByIban(newAccount.getIban())){
            myAccount.save(newAccount);
        }else{
            throw new RuntimeException("iBan-ul exista deja in baza de date!!");
        }
        AccountDTO accountToReturn = new AccountDTO(
                newAccount.getId(),
                newAccount.getName()
        );
        return accountToReturn;
    }
    @Transactional
    public void makeTransfer(String iban1, String iban2, BigDecimal amount){
        Account acc1 = myAccount.findAccountsByIban(iban1).orElse(null);
        Account acc2 = myAccount.findAccountsByIban(iban2).orElse(null);

        if(acc1==null || acc2==null){
            throw new RuntimeException("Nu exista acest iban pentru transfer!");
        }
        if(acc1.getAmount().compareTo(amount)<0){
            throw new RuntimeException("Nu ai bani suficienti!");
        }
        acc1.setAmount(acc1.getAmount().subtract(amount));
        acc2.setAmount(acc2.getAmount().add(amount));

        myAccount.save(acc1);
        myAccount.save(acc2);
        //TODO Kafka regula de transfer
        String message = String.format("{\"fromIban\":\"%s\", \"toIban\":\"%s\", \"amount\":%s}",
                iban1, iban2, amount);
        kafkaTemplate.send("transfer-topic",message);
    }

    public Account showAccount(String iban){
       return myAccount.findAccountsByIban(iban).
               orElseThrow(()->new RuntimeException("Contul cu iban-ul: "+iban +"nu exista"));
    }
}
