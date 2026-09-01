package com.dowinn.rouletto.dto.socket.response;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class PastResultDto {

    @JsonProperty("LAST_GAME_RESULT")
    private List<BallData> LAST_GAME_RESULT;
    @JsonProperty("COLD_OCC")
    private List<Long> COLD_OCC;
    @JsonProperty("HOT_OCC")
    private List<Long> HOT_OCC;
    @JsonProperty("EVEN")
    private BigDecimal EVEN;
    @JsonProperty("ODD")
    private BigDecimal ODD;
    @JsonProperty("COLD")
    private List<Integer> COLD;
    @JsonProperty("HOT")
    private List<Integer> HOT;
    @JsonProperty("SPOT")
    private List<Integer> SPOT;
    @JsonProperty("SPOT_OCC")
    private List<Long> SPOT_OCC;




    @Data
    public static class BallData{
        private List<Integer> balls;
        private String gameId;
    }
}
