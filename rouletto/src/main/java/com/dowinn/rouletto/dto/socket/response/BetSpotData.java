package com.dowinn.rouletto.dto.socket.response;

import com.dowinn.rouletto.dto.gateway.response.BetLimitResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BetSpotData {

    private BetLimitResponse betLimit;
    private List<Integer> variantId;
    private String tableId;
    private Double jackpotTicketStake;
    private Integer jackpotMaxTickets;
    private Map<Integer,String> spotLimit;
    private Integer rtp;
}
