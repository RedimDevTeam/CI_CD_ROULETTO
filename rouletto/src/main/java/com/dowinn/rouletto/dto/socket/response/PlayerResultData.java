package com.dowinn.rouletto.dto.socket.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlayerResultData {
    private String  gameId;
    private List<Integer> ball;
    private String lastResults;
    private PlayerData results;

    private Double totalBetAmount;
    private Double totalWinAmount;
    private Double jackPotWinAmount;
    private boolean progressiveJackPot;

    private Integer secs;
    private Integer baseSecs ;
    private Integer diff;

    @Data
    public static class PlayerData{
        private List<Integer> win;
        private Map<Integer,List<Integer>> ws;
        private Map<Integer,Map<String,Double>> wp;
        private Map<Integer,List<JackPotResult>> jackpotResult;
    }

    @Data
    public static class JackPotResult{
        private Long id;
        private List<Integer> balls;
        private Double stake;
        private Double payoff;
    }


}
