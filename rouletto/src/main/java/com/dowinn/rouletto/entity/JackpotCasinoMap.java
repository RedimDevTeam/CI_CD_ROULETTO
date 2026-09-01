package com.dowinn.rouletto.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "jackpotcasinomap", schema = "roulette")
public class JackpotCasinoMap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id",columnDefinition = "BIGINT")
    private Long id;

    @Column(name = "casinoid", length = 50)
    private String casinoId;

    @Column(name = "jackpotceiling")
    private Long jackpotCeiling;

    @Column(name="ticketAmount",columnDefinition = "numeric")
    private Double ticketAmount;

    @Column(name ="ticketcount")
    private Integer ticketCount;

    @ManyToOne
    @JoinColumn(name = "jackpotconfig", referencedColumnName = "id")
    private JackpotConfig jackpotConfig;
}