package com.dowinn.rouletto.entity;


import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "gamebetspots")
public class GameBetSpot {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name="tableid")
    private String tableId;

    @Column(name = "currency")
    private String currency;

    @ManyToOne(targetEntity = Spots.class,fetch = FetchType.EAGER)
    @JoinColumn(name = "betspotid", referencedColumnName = "id")
    private Spots spot;

    @Column(name = "betlimittypeid")
    private Integer betLimitTypeId;

    @Column(name = "casinoid")
    private String casinoId;

    @Column(name="min", columnDefinition = "numeric")
    private double min;

    @Column(name="max", columnDefinition = "numeric")
    private double max;


    @Column(name="percentage")
    private boolean percentage;


}
