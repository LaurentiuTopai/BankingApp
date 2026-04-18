package com.example.transactions.Service;

import com.example.transactions.Entity.Transaction;
import com.example.transactions.Entity.TransactionDTO;
import com.example.transactions.Repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TransactionService{
    private final TransactionRepository myTranRepository;
    @Autowired
    public TransactionService(TransactionRepository myTranRepository){
        this.myTranRepository = myTranRepository;
    }
    public TransactionDTO createTransaction(Transaction myTransaction){
        myTranRepository.save(myTransaction);
            TransactionDTO myDTO = new TransactionDTO(
                    myTransaction.getId(),
                    myTransaction.getFromIban(),
                    myTransaction.getToIban()
            );
            return myDTO;
    }


}
