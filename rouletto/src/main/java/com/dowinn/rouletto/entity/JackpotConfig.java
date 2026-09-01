package com.dowinn.rouletto.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Data
@Table(name = "jackpotconfig", schema = "roulette")
public class JackpotConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id",columnDefinition = "BIGINT")
    private Long id;

    @Column(name = "rtp")
    private Integer rtp;

    @Column(name = "jackpottype")
    private Integer jackpotType;

    @Column(name = "percentage", columnDefinition = "numeric")
    private Double percentage;

    @Column(name = "amount",  columnDefinition = "numeric")
    private Double amount;
}