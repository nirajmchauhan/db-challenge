package com.db.awmd.challenge.repository;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.exception.DuplicateAccountIdException;
import com.db.awmd.challenge.exception.InSufficientBalanceException;
import com.db.awmd.challenge.exception.NoAccountFoundException;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class AccountsRepositoryInMemory implements AccountsRepository {

    private final Map<String, Account> accounts = new ConcurrentHashMap<>();

    @Override
    public void createAccount(Account account) throws DuplicateAccountIdException {
        Account previousAccount = accounts.putIfAbsent(account.getAccountId(), account);
        if (previousAccount != null) {
            throw new DuplicateAccountIdException(
                    "Account id " + account.getAccountId() + " already exists!");
        }
    }

    @Override
    public Account getAccount(String accountId) {
        Account account = accounts.get(accountId);
        if (account == null) {
            throw new NoAccountFoundException("No account found with id: " + accountId);
        }
        return account;
    }

    @Override
    public void clearAccounts() {
        accounts.clear();
    }

    @Override
    public synchronized void transfer(Account fromAccount, Account toAccount, BigDecimal amount) throws InSufficientBalanceException {
        if (fromAccount.getBalance().subtract(amount).compareTo(amount) <= 0) {
            throw new InSufficientBalanceException(fromAccount.getAccountId() + " is having insufficient amount to transfer " + amount);
        }
        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        toAccount.setBalance(toAccount.getBalance().add(amount));
    }

}
