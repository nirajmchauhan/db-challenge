package com.db.awmd.challenge.exception;

public class InSufficientBalanceException extends RuntimeException {

    public InSufficientBalanceException(String message) {
        super(message);
    }
}
