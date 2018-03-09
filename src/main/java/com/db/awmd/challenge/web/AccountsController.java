package com.db.awmd.challenge.web;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.Transfer;
import com.db.awmd.challenge.exception.DuplicateAccountIdException;
import com.db.awmd.challenge.exception.InSufficientBalanceException;
import com.db.awmd.challenge.exception.NoAccountFoundException;
import com.db.awmd.challenge.service.AccountsService;
import com.db.awmd.challenge.service.EmailNotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/v1/accounts")
@Slf4j
public class AccountsController {

    private final AccountsService accountsService;
    private final EmailNotificationService emailNotificationService;

    @Autowired
    public AccountsController(AccountsService accountsService, EmailNotificationService emailNotificationService) {
        this.accountsService = accountsService;
        this.emailNotificationService = emailNotificationService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> createAccount(@RequestBody @Valid Account account) {
        log.info("Creating account {}", account);

        try {
            this.accountsService.createAccount(account);
        } catch (DuplicateAccountIdException daie) {
            return new ResponseEntity<>(daie.getMessage(), HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping(path = "/{accountId}")
    public Account getAccount(@PathVariable String accountId) {
        log.info("Retrieving account for id {}", accountId);
        return this.accountsService.getAccount(accountId);
    }

    @PostMapping(path = "/transfer-money", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> transferAmount(@RequestBody @Valid Transfer transfer) {
        log.info("Transferring amount");
        try {
            this.accountsService.transferAmount(transfer.getFromAccountId(), transfer.getToAccountId(), transfer.getAmount());
        } catch (InSufficientBalanceException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (NoAccountFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
        emailNotificationService.notifyAboutTransfer(accountsService.getAccount(transfer.getFromAccountId())
                , "Amount " + transfer.getAmount() + " was successfully credited from your account to " + transfer.getToAccountId());
        emailNotificationService.notifyAboutTransfer(accountsService.getAccount(transfer.getToAccountId())
                , "Amount " + transfer.getAmount() + " was successfully debited to your account by " + transfer.getFromAccountId());
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
