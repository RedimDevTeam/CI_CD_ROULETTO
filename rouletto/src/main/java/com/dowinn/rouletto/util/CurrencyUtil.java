package com.dowinn.rouletto.util;

import com.dowinn.rouletto.dto.gateway.response.CurrencyRatesResponse;
import com.dowinn.rouletto.redis.RedisHelper;
import com.dowinn.rouletto.service.GatewayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class CurrencyUtil {

    @Autowired
    GatewayService gatewayService;

    @Autowired
    RedisHelper redisHelper;

    public  BigDecimal getCurrencRates(Long playerId,String currency){
      if(redisHelper.checkCurrencyExists(currency)){
         return redisHelper.getCurrencyRate(currency);
      }
        ResponseEntity<BigDecimal> response = gatewayService.getCurrencyUsd(playerId);
        if(response.getStatusCode().is2xxSuccessful()){
           BigDecimal rate = response.getBody();
           redisHelper.setCurrencyUsdExchange(currency,rate);
           return rate;
       }
       return null;
    }
    public static BigDecimal baseToPlayerCurrencyConversion(BigDecimal amount,BigDecimal baseCurrency){
        return amount.multiply(baseCurrency);
    }


    public static BigDecimal baseToPlayerCurrencyConversion(BigDecimal amount){
        return baseToPlayerCurrencyConversion(amount,BigDecimal.ZERO);
    }

    public  BigDecimal getPlayerAmount(Long player,BigDecimal amount,String currecy){
        return baseToPlayerCurrencyConversion(amount,getCurrencRates(player,currecy)).setScale(2, RoundingMode.HALF_UP);
    }

}
