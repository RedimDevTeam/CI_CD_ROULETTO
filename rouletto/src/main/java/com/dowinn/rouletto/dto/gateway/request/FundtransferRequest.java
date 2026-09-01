package com.dowinn.rouletto.dto.gateway.request;

import com.dowinn.rouletto.enums.TransactionSubType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FundtransferRequest {
    private TransactionSubType type;
    private Long userId;
    private String currencyid;
    private BigDecimal amount;
    private String transactionid;
    private String reverseTransactionid;
    private String round;
    private String transactiontime;
    private String gamecode;
    private String token;

}