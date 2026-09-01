package com.dowinn.rouletto.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Entity
@Table(name = "jackpot", schema = "roulette")
public class Jackpot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "jid",columnDefinition = "BIGINT")
    private Long jid;

    @Column(name = "casino", length = 50)
    private String casinoId;

    @Column(name = "jackpotamount", columnDefinition = "numeric")
    private Double jackpotAmount;

    @Column(name = "createdat")
    private LocalDate createdAt;

    @Column(name = "playerid")
    private List<Long> playerId;

    @Column(name = "gameid")
    private String gameId;

    @Column(name = "tableid")
    private String tableId;

    @Column(name = "active")
    private boolean active;
}