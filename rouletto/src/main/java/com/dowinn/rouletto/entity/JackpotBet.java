package com.dowinn.rouletto.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Entity
@Table(name = "jackpotbet", schema = "roulette")
public class JackpotBet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id",columnDefinition = "BIGINT")
    private Long id;

    @Column(name = "playerid")
    private Long playerId;

    @Column(name = "casino", length = 50)
    private String casino;

    @Column(name = "amount", columnDefinition = "numeric")
    private Double amount;

    @Column(name = "payoff",columnDefinition = "numeric")
    private Double payoff;

    @Column(name = "tableid")
    private String tableId;

    @Column(name = "gameid")
    private String gameId;

    @Column(name = "createdat")
    private LocalDate createdAt;

    @Column(name = "jackpotid")
    private Long jackpotId;

    @Column(name = "rtp")
    private Integer rtp;

    @Column(name = "bet_numbers")
    private List<Integer>  betNumbers;

    @Column(name="currency")
    private String currency;
}