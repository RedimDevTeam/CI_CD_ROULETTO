package com.dowinn.rouletto.entity;


import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "userid")
    private Long playerId;

    @Column(name = "amount" ,columnDefinition = "numeric")
    private Double amount;

    @Column(name = "gameid")
    private String gameId;

    @Column(name = "tableid")
    private  String tableId;

    @Column(name = "currencyid")
    private String currencyId;

    @Column(name = "transactionid")
    private String transactionStatus;

    @Column(name = "txnsubtypeid")
    private Integer transactionSubTypeId;

    @Column(name ="bets")
    public String betIds;

    @CreationTimestamp
    @Column(name ="transactiontime")
    private LocalDateTime transactionTime;
}
