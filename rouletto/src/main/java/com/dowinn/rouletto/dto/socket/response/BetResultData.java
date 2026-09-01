package com.dowinn.rouletto.dto.socket.response;


import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class BetResultData {
    private Double balance;
    private Map<Integer,Double> betedSpots;
    private List<List<Integer>> jackPot;
}
