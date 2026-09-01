package com.dowinn.rouletto.dto.socket.request;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class BetRequestDto {
    private Map<Integer,Double> betSpots;
    private String gameId;
    private List<List<Integer>> jackPot;
}
