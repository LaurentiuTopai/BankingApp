package com.example.transactions.Controller;

import com.example.transactions.Entity.Transaction;
import com.example.transactions.Entity.TransactionStatus;
import com.example.transactions.Entity.TransactionType;
import com.example.transactions.Service.TransactionService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
public class TransactionConsumer {
    private final TransactionService myService;
    private final ObjectMapper objectMapper; //Pt conversie JSON in object
    public TransactionConsumer(TransactionService myService,
                               ObjectMapper objectMapper){
        this.myService = myService;
        this.objectMapper = objectMapper;
    }
    @KafkaListener(topics = "transfer-topic",groupId = "transactions-group")
    public void consume(String message){
        try{
            Transaction transaction = objectMapper.readValue(message,Transaction.class);
            transaction.setType(TransactionType.TRANSFER);
            transaction.setStatus(TransactionStatus.SUCCESS);
            myService.createTransaction(transaction);
            System.out.println("Tranzactie salvata automat de KAFKA: "+message);
        }catch(Exception e){
            System.err.println("Erroare de procesare de KAFKA: "+e.getMessage());
        }
    }
}
