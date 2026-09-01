package com.dowinn.rouletto.model;


import com.dowinn.rouletto.entity.Spots;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class GameResult {

    private List<Integer> winningSpot;
    private Map<Integer,Integer> spotBallCount;//spotIndex :no of balls

    private List<Spots> spots;
}
