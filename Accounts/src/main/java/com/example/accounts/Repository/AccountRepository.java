package com.example.accounts.Repository;

import com.example.accounts.Entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account,Long> {
    Optional<Account> findAccountsById(Long id);
    Optional<Account> findAccountsByIban(String iban);

    boolean existsAccountsByIban(String iban);
    boolean existsAccountsById(Long id);
}
