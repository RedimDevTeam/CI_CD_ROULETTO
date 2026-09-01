package com.dowinn.rouletto.dto.api.response;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GameHistoryResponseDto {

    private String gameId;
    private LocalDateTime gameOn;
    private List<BetSpotDetails> mainbet;
    private List<BetSpotDetails> sideBet;
    private List<JackPotDetails> jackPotBet;
    private List<Integer> ballNumbers;
    private List<String> winSpots;
    private BigDecimal totalWinAmount;
    private BigDecimal totalBetAmount;
    private String gameUrl;
    private String status;


    @Data
    public static class BetSpotDetails{
        private Integer id;
        private String spotName;
        private BigDecimal stake;
        private BigDecimal payOff;


    }

    @Data
    public static class JackPotDetails{
        private Long id;
        private List<Integer> ballNumbers;
        private BigDecimal stake;
        private BigDecimal payOff;

    }
}
