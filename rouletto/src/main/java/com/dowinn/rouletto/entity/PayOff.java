package com.dowinn.rouletto.entity;


import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

@Entity
@Table(name="payoff")
@Data
public class PayOff {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rtp")
    private Integer rtp;


    @Column(name = "payoff", columnDefinition = "numeric")
    private Double payoff;

    @Column(name = "multipayoff")
    private String multiPayOff;

    @ManyToOne(targetEntity = Spots.class,fetch = FetchType.EAGER)
    @JoinColumn(name = "spot",referencedColumnName ="index")
    @JsonBackReference
    @ToString.Exclude
    private Spots spots;
}
