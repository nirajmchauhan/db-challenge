package com.db.awmd.challenge.service;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.repository.AccountsRepository;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class AccountsService {

  @Getter
  private final AccountsRepository accountsRepository;

  @Autowired
  public AccountsService(AccountsRepository accountsRepository) {
    this.accountsRepository = accountsRepository;
  }

  public void createAccount(Account account) {
    this.accountsRepository.createAccount(account);
  }

  public void transferAmount(String fromAccountId, String toAccountId, BigDecimal amount){
    Account fromAccount = this.accountsRepository.getAccount(fromAccountId);
    Account toAccount = this.accountsRepository.getAccount(toAccountId);
    if(fromAccount != null && toAccount != null) {
      this.accountsRepository.transfer(fromAccount, toAccount, amount);
      return;
    }
  }

  public Account getAccount(String accountId) {
    return this.accountsRepository.getAccount(accountId);
  }
}
