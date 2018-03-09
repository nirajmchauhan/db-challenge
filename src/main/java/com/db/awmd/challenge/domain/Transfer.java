package com.db.awmd.challenge.domain;

import com.db.awmd.challenge.exception.DuplicateAccountIdException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class Transfer {

    @NotNull
    @NotEmpty
    private final String fromAccountId;

    @NotNull
    @NotEmpty
    private final String toAccountId;

    @NotNull
    @Min(value = 1, message = "Transfer amount should be greater than 0")
    private BigDecimal amount;

    @JsonCreator
    public Transfer(@JsonProperty("fromAccountId") String fromAccountId,
                    @JsonProperty("toAccountId") String toAccountId,
                    @JsonProperty("amount") BigDecimal amount) {
        if(fromAccountId.equals(toAccountId)){
            throw new DuplicateAccountIdException("From & To account cannot be same.");
        }
        this.fromAccountId = fromAccountId;
        this.toAccountId = toAccountId;
        this.amount = amount;
    }
}