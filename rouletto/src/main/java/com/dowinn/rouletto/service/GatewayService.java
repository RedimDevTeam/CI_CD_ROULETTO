package com.dowinn.rouletto.service;

import com.dowinn.rouletto.communication.APIResponse;
import com.dowinn.rouletto.dto.gateway.request.FundtransferRequest;
import com.dowinn.rouletto.dto.gateway.response.BetLimitResponse;
//import com.dowinn.rouletto.dto.gateway.response.CurrencyRatesResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;

@FeignClient(name = "winball-api-gateway", url = "${gateway.url}")
public interface GatewayService {

    @PostMapping("/partner/api/player/fundTransfer")
    public ResponseEntity<APIResponse> fundTransfer(@RequestBody FundtransferRequest fundtransferRequest);

    @GetMapping("/partner/api/player/balance/{integrationType}/{userId}/{sessionId}")
    public ResponseEntity<APIResponse> getBalance(@PathVariable String integrationType, @PathVariable long userId, @PathVariable String sessionId);


    @GetMapping("/casino/api/betLimit/{tableId}/{betLimitTypeId}/{casinoId}/{currency}")
    public ResponseEntity<BetLimitResponse> betLimitResponse(@PathVariable String tableId,
                                                             @PathVariable int betLimitTypeId,
                                                             @PathVariable String casinoId,
                                                             @PathVariable String currency);

    @GetMapping("/casino/api/currency/{playerId}")
    public ResponseEntity<BigDecimal> getCurrencyUsd(@PathVariable Long playerId);

}
