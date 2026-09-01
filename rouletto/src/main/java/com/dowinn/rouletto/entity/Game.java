package com.dowinn.rouletto.entity;


import jakarta.persistence.*;
import lombok.Data;


@Entity
@Table(name = "games")
@Data
public class Game {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "statusid")
    private Integer status;

    @Column(name="shoeid")
    private Long shoe;

    @Column(name = "dealerid")
    private Long dealerId;

    @Column(name = "notes")
    private String notes;

    @Column(name = "tableid")
    private String tableId;

}


