package com.dowinn.rouletto.service;


import com.dowinn.rouletto.entity.Transaction;
import com.dowinn.rouletto.enums.TransactionSubType;
import com.dowinn.rouletto.model.GameDetail;
import com.dowinn.rouletto.repository.TransactionRepository;
import jakarta.persistence.Column;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TransactionService {


    @Autowired
    TransactionRepository transactionRepository;

    private Transaction createTransaction(Long player, double totalWinAmount, GameDetail gameDetail, String currency) {
        Transaction transaction=new Transaction();
        transaction.setTransactionStatus((totalWinAmount>0? TransactionSubType.WIN.name():TransactionSubType.LOSS.name()));
        transaction.setTransactionSubTypeId(-1);
        transaction.setAmount(totalWinAmount);
        transaction.setPlayerId(player);
        transaction.setGameId(gameDetail.getGameId());
        transaction.setTableId(gameDetail.getTableId());
        transaction.setCurrencyId(currency);
        transaction=transactionRepository.save(transaction);
        return transaction;
    }
}
