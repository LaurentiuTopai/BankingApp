package com.example.accounts.Service;

import com.example.accounts.Entity.User;
import com.example.accounts.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class UserService implements UserDetailsService{
    @Autowired
    private UserRepository myRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = myRepository.findByUsername(username)
                .orElseThrow(()->new RuntimeException("Userul nu a fost gasit"));

        System.out.println("Username in DB: " + user.getUsername());
        System.out.println("Password in DB: " + user.getPassword());

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                new ArrayList<>()
        );
    }
    public User register(String username,String password,Long accoundId){
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        return myRepository.save(user);
    }
}
