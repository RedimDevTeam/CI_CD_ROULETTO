package com.dowinn.rouletto.entity;


import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "tableconfigs")
@Data
public class TableConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "tableid")
    private String tableId;

    @Column(name = "timers")
    private String timers;

    @Column(name="active")
    private boolean active;
}
