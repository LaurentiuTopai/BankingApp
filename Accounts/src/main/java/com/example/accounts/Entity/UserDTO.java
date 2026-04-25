package com.example.accounts.Entity;

import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

public record UserDTO(String username,
                      String password,
                      String name,
                      int age,
                      String iban,
                      BigDecimal amount) {
}
