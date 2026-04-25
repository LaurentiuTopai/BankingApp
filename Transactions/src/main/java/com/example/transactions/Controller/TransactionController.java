package com.example.transactions.Controller;

import com.example.transactions.Entity.Transaction;
import com.example.transactions.Entity.TransactionDTO;
import com.example.transactions.Service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/transactions")
public class TransactionController {
    private final TransactionService myService;
    @Autowired
    public TransactionController(TransactionService myService){
        this.myService = myService;
    }
    @PostMapping
    public ResponseEntity<TransactionDTO> createTransaction(@RequestBody Transaction newTransaction){
        try{
            TransactionDTO myDTO = myService.createTransaction(newTransaction);
            return new ResponseEntity<>(myDTO,HttpStatus.CREATED);
        }catch(Exception e){
            e.printStackTrace();
            return new ResponseEntity<>((HttpHeaders) null, HttpStatus.BAD_REQUEST);
        }
    }
}
