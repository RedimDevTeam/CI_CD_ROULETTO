package com.dowinn.rouletto.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Data
@Entity
@Table(name = "shoes", schema = "roulette")
public class Shoes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "active")
    private Boolean active;

    // timestamp with time zone
    @Column(name = "endtime")
    private LocalDateTime endTime;

    // timestamp without time zone
    @Column(name = "starttime")
    private LocalDateTime startTime;

    @Column(name = "tableid")
    private String tableId;
}