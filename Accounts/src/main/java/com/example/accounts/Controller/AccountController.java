package com.example.accounts.Controller;


import com.example.accounts.Service.AccountService;
import org.hibernate.action.internal.EntityAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.accounts.Entity.*;

import java.math.BigDecimal;
@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/accounts")
public class AccountController {
    private  final AccountService myAccountService;
    @Autowired
    public AccountController(AccountService myAccountService){
        this.myAccountService = myAccountService;
    }


    @PostMapping
    public ResponseEntity<AccountDTO> createAccount(@RequestBody Account newAccount){
        try{
            AccountDTO accToReturn = myAccountService.createAccount(newAccount);
            return new ResponseEntity<>(accToReturn,HttpStatus.CREATED);
        }catch(Exception e){
            e.printStackTrace();
            return new ResponseEntity<>((HttpHeaders) null,HttpStatus.BAD_REQUEST);
        }
    }
    @GetMapping
    public ResponseEntity<Account> showAccountDetails(@RequestParam String iban){
        try{
            Account toReturn = myAccountService.showAccount(iban);
            return new ResponseEntity<>(toReturn,HttpStatus.OK);
        }catch(Exception e){
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    @PostMapping("/transfer")
    public ResponseEntity<String> makeTransfer(@RequestBody TransferRequest myTransfer){
        try{
            myAccountService.makeTransfer(
                    myTransfer.fromIban(),
                    myTransfer.toIban(),
                    myTransfer.amount()
            );
            return ResponseEntity.ok("Transfer efectuat cu succes");
        }catch(Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Erroare "+e.getMessage());
        }
    }
}
