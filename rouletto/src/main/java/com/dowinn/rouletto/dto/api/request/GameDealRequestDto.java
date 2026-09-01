package com.dowinn.rouletto.dto.api.request;

import lombok.Data;

import java.util.List;

@Data
public class GameDealRequestDto {

    private String gameId;
    private String tableId;
    private List<Integer> balls;
}
