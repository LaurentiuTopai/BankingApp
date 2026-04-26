package com.example.accounts.Tests;
import com.example.accounts.Entity.Account;
import com.example.accounts.Entity.AccountDTO;
import com.example.accounts.Repository.AccountRepository;
import com.example.accounts.Service.AccountService;
import kafka.Kafka;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {
        @Mock
        private AccountRepository accountRepository;

        @Mock
        private KafkaTemplate<String,String> kafkaTemplate;

        @InjectMocks
        private AccountService accountService;

        @Test
        public void testCreateAccount_Succes(){
            Account newAccount = new Account();
            newAccount.setId(1L);
            newAccount.setIban("RO123BANC");
            newAccount.setName("Lau");
            newAccount.setAge(21);
            newAccount.setAmount(new BigDecimal("100.0"));
            ///Return false ca daca exista deja atunci nu-i bine
            when(accountRepository.existsAccountsByIban("RO123BANC")).thenReturn(false);
            
            AccountDTO result = accountService.createAccount(newAccount);

            assertNotNull(result);
            assertEquals("Lau",result.name());
            assertEquals(1L,result.id());
            verify(accountRepository,times(1)).save(newAccount);
        }
        @Test
        public void testMakeTransfer_Succes(){
            Account contExpeditor = new Account();
            contExpeditor.setIban("RO_EXPEDITOR");
            contExpeditor.setAmount(new BigDecimal("1000.0"));

            Account contDestinatar = new Account();
            contDestinatar.setIban("RO_DESTINATAR");
            contDestinatar.setAmount(new BigDecimal("500.0"));

            when(accountRepository.findAccountsByIban("RO_EXPEDITOR")).thenReturn(Optional.of(contExpeditor));
            when(accountRepository.findAccountsByIban("RO_DESTINATAR")).thenReturn(Optional.of(contDestinatar));

            accountService.makeTransfer("RO_EXPEDITOR", "RO_DESTINATAR", new BigDecimal("200.0"));

            assertEquals(new BigDecimal("800.0"), contExpeditor.getAmount());
            assertEquals(new BigDecimal("700.0"), contDestinatar.getAmount());

            verify(accountRepository, times(1)).save(contExpeditor);
            verify(accountRepository, times(1)).save(contDestinatar);
            verify(kafkaTemplate, times(1)).send(eq("transfer-topic"), anyString());
    }

}
