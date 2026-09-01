package com.dowinn.rouletto.dto.gateway.response;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CurrencyRatesResponse {
    private Long id;
    private String currencyCode;
    private BigDecimal currencyValue;
    private LocalDateTime createdDate;
}
