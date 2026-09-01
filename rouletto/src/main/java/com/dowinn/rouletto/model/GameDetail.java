package com.dowinn.rouletto.model;

import com.dowinn.rouletto.enums.GameStatus;
import com.dowinn.rouletto.enums.Timers;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data

public class GameDetail {

    private String tableId;
    private String gameId;
    private String machineId;
    private String machineGameId;
    private List<Integer> balls;
    private GameStatus status;
    private Map<Timers,Integer> timers;
    private GameResult gameResult;
    private LocalDateTime starts;
    private LocalDateTime end;

}
