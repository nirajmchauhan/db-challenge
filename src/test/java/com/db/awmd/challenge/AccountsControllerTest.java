package com.db.awmd.challenge;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.service.AccountsService;
import com.db.awmd.challenge.service.EmailNotificationService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@RunWith(SpringRunner.class)
@SpringBootTest
@WebAppConfiguration
public class AccountsControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private AccountsService accountsService;

    @Autowired
    private EmailNotificationService emailNotificationService;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Before
    public void prepareMockMvc() {
        this.mockMvc = webAppContextSetup(this.webApplicationContext).build();

        // Reset the existing accounts before each test.
        accountsService.getAccountsRepository().clearAccounts();
    }

    @Test
    public void createAccount() throws Exception {
        this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
                .content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isCreated());

        Account account = accountsService.getAccount("Id-123");
        assertThat(account.getAccountId()).isEqualTo("Id-123");
        assertThat(account.getBalance()).isEqualByComparingTo("1000");
    }

    @Test
    public void createDuplicateAccount() throws Exception {
        this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
                .content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isCreated());

        this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
                .content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isBadRequest());
    }

    @Test
    public void createAccountNoAccountId() throws Exception {
        this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
                .content("{\"balance\":1000}")).andExpect(status().isBadRequest());
    }

    @Test
    public void createAccountNoBalance() throws Exception {
        this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
                .content("{\"accountId\":\"Id-123\"}")).andExpect(status().isBadRequest());
    }

    @Test
    public void createAccountNoBody() throws Exception {
        this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void createAccountNegativeBalance() throws Exception {
        this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
                .content("{\"accountId\":\"Id-123\",\"balance\":-1000}")).andExpect(status().isBadRequest());
    }

    @Test
    public void createAccountEmptyAccountId() throws Exception {
        this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
                .content("{\"accountId\":\"\",\"balance\":1000}")).andExpect(status().isBadRequest());
    }

    @Test
    public void getAccount() throws Exception {
        String uniqueAccountId = "Id-" + System.currentTimeMillis();
        Account account = new Account(uniqueAccountId, new BigDecimal("123.45"));
        this.accountsService.createAccount(account);
        this.mockMvc.perform(get("/v1/accounts/" + uniqueAccountId))
                .andExpect(status().isOk())
                .andExpect(
                        content().string("{\"accountId\":\"" + uniqueAccountId + "\",\"balance\":123.45}"));
    }

    @Test
    public void transferMoneyWithinAccounts() throws Exception {
        String fromAccountId = "Id-" + UUID.randomUUID();
        String toAccountId = "Id-" + UUID.randomUUID();
        Account fromAccount = new Account(fromAccountId, new BigDecimal("5453.56"));
        Account toAccount = new Account(toAccountId, new BigDecimal("200.08"));
        this.accountsService.createAccount(fromAccount);
        this.accountsService.createAccount(toAccount);

        this.mockMvc.perform(post("/v1/accounts/transfer-money").contentType(MediaType.APPLICATION_JSON)
                .content("{\"fromAccountId\":\"" + fromAccountId + "\", \"toAccountId\":\"" + toAccountId + "\", \"amount\":\"2000.00\"}")).andExpect(status().isOk());

        assertThat(fromAccount.getBalance()).isEqualByComparingTo("3453.56");
        assertThat(toAccount.getBalance()).isEqualByComparingTo("2200.08");
    }

    @Test
    public void transferMoneyWithInSufficientFunds() throws Exception {
        String fromAccountId = "Id-" + UUID.randomUUID();
        String toAccountId = "Id-" + UUID.randomUUID();
        Account fromAccount = new Account(fromAccountId, new BigDecimal("1453.56"));
        Account toAccount = new Account(toAccountId, new BigDecimal("200.08"));
        this.accountsService.createAccount(fromAccount);
        this.accountsService.createAccount(toAccount);

        this.mockMvc.perform(post("/v1/accounts/transfer-money").contentType(MediaType.APPLICATION_JSON)
                .content("{\"fromAccountId\":\"" + fromAccountId + "\", \"toAccountId\":\"" + toAccountId + "\", \"amount\":\"2000.00\"}")).andExpect(status().is5xxServerError());

        assertThat(fromAccount.getBalance()).isEqualByComparingTo("1453.56");
        assertThat(toAccount.getBalance()).isEqualByComparingTo("200.08");
    }

    @Test
    public void transferMoneyWithNegativeAmountValue() throws Exception {
        String fromAccountId = "Id-" + UUID.randomUUID();
        String toAccountId = "Id-" + UUID.randomUUID();
        Account fromAccount = new Account(fromAccountId, new BigDecimal("5453.56"));
        Account toAccount = new Account(toAccountId, new BigDecimal("200.08"));
        this.accountsService.createAccount(fromAccount);
        this.accountsService.createAccount(toAccount);

        this.mockMvc.perform(post("/v1/accounts/transfer-money").contentType(MediaType.APPLICATION_JSON)
                .content("{\"fromAccountId\":\"" + fromAccountId + "\", \"toAccountId\":\"" + toAccountId + "\", \"amount\":\"-2000.00\"}"))
                .andExpect(status().isBadRequest());

        assertThat(fromAccount.getBalance()).isEqualByComparingTo("5453.56");
        assertThat(toAccount.getBalance()).isEqualByComparingTo("200.08");
    }

    @Test
    public void transferMoneyWithWrongFromAccountId() throws Exception {
        String fromAccountId = "Id-" + UUID.randomUUID();
        String toAccountId = "Id-" + UUID.randomUUID();
        Account fromAccount = new Account(fromAccountId, new BigDecimal("5453.56"));
        Account toAccount = new Account(toAccountId, new BigDecimal("200.08"));
        this.accountsService.createAccount(fromAccount);
        this.accountsService.createAccount(toAccount);

        this.mockMvc.perform(post("/v1/accounts/transfer-money").contentType(MediaType.APPLICATION_JSON)
                .content("{\"fromAccountId\":\"id-yut\", \"toAccountId\":\"" + toAccountId + "\", \"amount\":\"2000.00\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void transferMoneyWithWrongToAccountId() throws Exception {
        String fromAccountId = "Id-" + UUID.randomUUID();
        String toAccountId = "Id-" + UUID.randomUUID();
        Account fromAccount = new Account(fromAccountId, new BigDecimal("5453.56"));
        Account toAccount = new Account(toAccountId, new BigDecimal("200.08"));
        this.accountsService.createAccount(fromAccount);
        this.accountsService.createAccount(toAccount);

        this.mockMvc.perform(post("/v1/accounts/transfer-money").contentType(MediaType.APPLICATION_JSON)
                .content("{\"fromAccountId\":\"" + fromAccountId + "\", \"toAccountId\":\"id-123\", \"amount\":\"2000.00\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void transferMoneyWithinSameAccounts() throws Exception {
        String fromAccountId = "Id-" + UUID.randomUUID();
        this.mockMvc.perform(post("/v1/accounts/transfer-money").contentType(MediaType.APPLICATION_JSON)
                .content("{\"fromAccountId\":\"" + fromAccountId + "\", \"toAccountId\":\"" + fromAccountId + "\", \"amount\":\"2000.00\"}"))
                .andExpect(status().isBadRequest());

    }
}
