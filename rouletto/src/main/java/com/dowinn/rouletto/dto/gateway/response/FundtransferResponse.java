package com.dowinn.rouletto.dto.gateway.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FundtransferResponse {
    private String status;
    private String type;
    private String playerId;
    private String username;
    private String currencyId;
    private BigDecimal amount;
    private BigDecimal balance;
    private String partnertxnid;
    private String transactionid;
}