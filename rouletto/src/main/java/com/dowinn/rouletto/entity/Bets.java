package com.dowinn.rouletto.entity;


import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "bets")
@Data
public class Bets {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id",columnDefinition = "BIGINT")
    private Long Id;

    @Column(name = "userid")
    private Long playerId;

    @Column(name = "gameid")
    private String gameId;

    @Column(name = "betspotid")
    private Integer spotIndex;

    @Column(name = "stake" ,columnDefinition = "numeric")
    private Double amount;

    @Column(name = "payoff", columnDefinition = "numeric")
    private Double realPayoff;

    @Column(name="variantid")
    private Integer variantId;

    @Column(name = "betlimittypeid")
    private Integer betLimit;

    @Column(name="casinoid",columnDefinition = "varchar")
    private String casinoId;

    @Column(name = "currency")
    private String currency;

    @Column(name = "rtp")
    private Integer rtp;

    @Column(name = "tableid")
    private String tableId;

}
