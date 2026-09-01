package com.dowinn.rouletto.model;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;


@Data
public class PlayerBetHistory {

    private String gameId;
    private BigDecimal amount;
    private BigDecimal payoff;
    private Long betId;
    private Integer status;
    private String winSpots;
    private List<Integer> spot;
    private List<Integer> results;
    private Integer gameType;
    private LocalDateTime gameon;
    private String gameUrl;

    // Getters and Setters


}