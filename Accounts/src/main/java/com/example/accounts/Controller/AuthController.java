package com.example.accounts.Controller;


import com.example.accounts.Configurations.JwtUtil;
import com.example.accounts.Entity.Account;
import com.example.accounts.Entity.User;
import com.example.accounts.Entity.UserDTO;
import com.example.accounts.Repository.AccountRepository;
import com.example.accounts.Repository.UserRepository;
import com.example.accounts.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private UserService myUserService;
    @Autowired
    private AuthenticationManager myManager;
    @Autowired
    private AccountRepository accRepo;
    @Autowired
    private UserRepository userRepo;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody UserDTO myDTO){
        try{
            Account account = new Account();
            account.setName(myDTO.name());
            account.setAge(myDTO.age());
            account.setIban(myDTO.iban());
            account.setAmount(myDTO.amount());
            accRepo.save(account);
            User user = new User();
            user.setUsername(myDTO.username());
            user.setPassword(passwordEncoder.encode(myDTO.password()));
            user.setAccount(account);
            userRepo.save(user);
            return new ResponseEntity<>("Succes",HttpStatus.CREATED);
        }catch(Exception e){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody User myUser){
        try{
            myManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            myUser.getUsername(),
                            myUser.getPassword()
                    )
            );
            String token = jwtUtil.generateToken(myUser.getUsername());
            return new ResponseEntity<>(token,HttpStatus.OK);
        }catch(Exception e){
            System.out.println("Erroarea de login: "+ e.getMessage());
            System.out.println("Tip erroare:"+e.getClass().getName());
            return new ResponseEntity<>("Credentiale gresite!",HttpStatus.UNAUTHORIZED);
        }
    }
}
