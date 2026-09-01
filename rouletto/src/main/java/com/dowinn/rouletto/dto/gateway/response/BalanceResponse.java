package com.dowinn.rouletto.dto.gateway.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BalanceResponse {
    private String type;
    private String username;
    private String currencyid;
    private BigDecimal amount;
    private String status;
}

