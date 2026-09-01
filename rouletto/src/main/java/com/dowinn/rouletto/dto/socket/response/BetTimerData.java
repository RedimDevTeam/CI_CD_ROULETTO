package com.dowinn.rouletto.dto.socket.response;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BetTimerData {

    private String gameId;
    private Integer secs;
    private Integer baseSecs;
    private Integer diff;

}
