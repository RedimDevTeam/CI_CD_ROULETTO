package com.dowinn.rouletto.service;


import com.dowinn.rouletto.communication.APIResponse;
import com.dowinn.rouletto.communication.StatusCode;
import com.dowinn.rouletto.dto.gateway.request.FundtransferRequest;
import com.dowinn.rouletto.dto.gateway.response.FundtransferResponse;
import com.dowinn.rouletto.entity.Bets;
import com.dowinn.rouletto.entity.JackpotBet;
import com.dowinn.rouletto.entity.Transaction;
import com.dowinn.rouletto.enums.TransactionSubType;
import com.dowinn.rouletto.model.GameDetail;
import com.dowinn.rouletto.repository.TransactionRepository;
import com.dowinn.rouletto.util.FunctionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FundService {

    @Autowired
    GatewayService gatewayService;

    @Autowired
    TransactionRepository transactionRepository;

    public FundtransferResponse handlePartnerTransaction(Double amount, Long playerId, GameDetail gameDetail, String currency, TransactionSubType transactionSubType, Transaction transaction) {


        try {

            FundtransferResponse fundTransferResponse = null;
            switch (transactionSubType) {
                case BET -> fundTransferResponse = placeBet(BigDecimal.valueOf(amount), playerId, gameDetail.getTableId(), gameDetail.getGameId(), transaction.getId());
                case WIN, LOSS -> fundTransferResponse = settlePayOff(BigDecimal.valueOf(amount), playerId, gameDetail.getTableId(), gameDetail.getGameId(), transaction.getId());
                case CANCEL -> fundTransferResponse = settleCancel(BigDecimal.valueOf(amount), playerId, gameDetail.getTableId(), gameDetail.getGameId(), transaction.getId());
            }

            if (fundTransferResponse != null && "0".equals(fundTransferResponse.getStatus())) {
                transaction.setTransactionSubTypeId(0);
                transactionRepository.save(transaction);
                return fundTransferResponse;
            }
        } catch (Exception e) {
            log.info("transaction exception {}", e);
        }
        log.info("transaction failed for player :: {}, amount :: {} ,game :: {},transactionid :: {} ", playerId, amount, gameDetail.getGameId(), transaction.getId());
        return null;
    }

    public FundtransferResponse placeBet(BigDecimal betAmount, Long playerId, String tableId, String gameId, Long transactionId) {

        FundtransferRequest fundtransferRequest = createFundRequest(betAmount, TransactionSubType.BET, playerId, transactionId, tableId, gameId);
        log.info("fund transfer request " + fundtransferRequest);
        try {
            ResponseEntity<APIResponse> response = gatewayService.fundTransfer(fundtransferRequest);
            log.info("fundTransfer Response {}", response);
            APIResponse apiResponse = response.getBody();
            if (apiResponse.getStatus() == StatusCode.SUCCESS) {
                return FunctionUtil.Deserialize(apiResponse.getResult(), FundtransferResponse.class);
            } else {
                return null;
            }
        } catch (Exception e) {
            log.info("exception e {}", e);
        }

        return null;
    }

    public FundtransferResponse settlePayOff(BigDecimal amount, Long playerId, String tableId, String gameId, Long transactionId) {
        FundtransferRequest fundtransferRequest = createFundRequest(amount,
                amount.compareTo(BigDecimal.ZERO) == 0 ? TransactionSubType.LOSS : TransactionSubType.WIN, playerId, transactionId, tableId, gameId);
        System.out.println("payoff request " + fundtransferRequest);
        try {
            ResponseEntity<APIResponse> response = gatewayService.fundTransfer(fundtransferRequest);
            log.info("payoff response {}", response);
            APIResponse apiResponse = response.getBody();

            if (apiResponse.getStatus() == StatusCode.SUCCESS) {
                return FunctionUtil.Deserialize(apiResponse.getResult(), FundtransferResponse.class);
            } else {
                return null;
            }
        } catch (Exception e) {
            log.info("exception e {}", e);
        }

        return null;
    }


    public FundtransferResponse settleCancel(BigDecimal amount, Long playerId, String tableId, String gameId, Long transactionId) {
        FundtransferRequest fundtransferRequest = createFundRequest(amount,
                TransactionSubType.CANCEL, playerId, transactionId, tableId, gameId);
        System.out.println("fund transfer request " + fundtransferRequest);
        try {
            ResponseEntity<APIResponse> response = gatewayService.fundTransfer(fundtransferRequest);
            log.info("fundTransfer Response {}", response);
            APIResponse apiResponse = response.getBody();
            if (apiResponse.getStatus() == StatusCode.SUCCESS) {
                return FunctionUtil.Deserialize(apiResponse.getResult(), FundtransferResponse.class);
            } else {
                return null;
            }
        } catch (Exception e) {
            log.info("exception e {}", e);
        }

        return null;
    }

    private FundtransferRequest createFundRequest(BigDecimal amount, TransactionSubType transactionSubType, Long playerId, Long transactionId, String tableId, String roundId) {
        FundtransferRequest fundtransferRequest = FundtransferRequest.builder().
                token(null)
                .amount(amount)
                .reverseTransactionid(null)
                .transactionid(String.valueOf(transactionId))
                .gamecode(tableId)
                .round(roundId)
                .type(transactionSubType)
                .userId(playerId)
                .transactiontime(String.valueOf(System.currentTimeMillis()))
                .build();
        return fundtransferRequest;
    }


    public Transaction createTransaction(Long player, double totalWinAmount,GameDetail gameDetail, String currency, TransactionSubType transactionSubType) {
        Transaction transaction = new Transaction();
        if (TransactionSubType.WIN == transactionSubType) {
            transaction.setTransactionStatus((totalWinAmount > 0 ? TransactionSubType.WIN.name() : TransactionSubType.LOSS.name()));
        } else {
            transaction.setTransactionStatus(transactionSubType.name());
        }

        transaction.setTransactionSubTypeId(-1);
        transaction.setAmount(totalWinAmount);
        transaction.setPlayerId(player);
        transaction.setGameId(gameDetail.getGameId());
        transaction.setTableId(gameDetail.getTableId());
        transaction.setCurrencyId(currency);
        transaction = transactionRepository.save(transaction);
        return transaction;
    }

    public void updateTransaction(Transaction transaction, List<Bets> bets, List<JackpotBet> jackPot){

        String playerBet = bets.stream().map(a -> String.valueOf(a.getId())).collect(Collectors.joining(","));
        String playerJackPotBet = "";
        if(jackPot!=null){
             playerJackPotBet = jackPot.stream().map(a -> String.valueOf(a.getId())).collect(Collectors.joining(","));
        }
        String player="bet:{"+playerBet+"}"+"jackpot:{"+playerJackPotBet+"}";
        transaction.setBetIds(player);
        transaction.setTransactionSubTypeId(0);
        transactionRepository.save(transaction);
    }

}
