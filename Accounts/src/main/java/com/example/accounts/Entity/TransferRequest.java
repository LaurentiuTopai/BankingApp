package com.example.accounts.Entity;

import java.math.BigDecimal;

public record TransferRequest(String fromIban, String toIban, BigDecimal amount) {}
