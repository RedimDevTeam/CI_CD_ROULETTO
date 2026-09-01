package com.dowinn.rouletto.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "gameresults")
public class GameResults {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "gameid")
    private String gameId;

    @Column(name = "tableid")
    private String tableId;

    @Column(name = "shoeid")
    private Long shoeId;

    @Column(name = "statusid")
    private Integer statusId;

    @Column(name = "winspots")
    private String winSpots;

    @Column(name="ballnumber")
    private List<Integer> ballNumbers;

    @Column(name ="url")
    private String url;
}
