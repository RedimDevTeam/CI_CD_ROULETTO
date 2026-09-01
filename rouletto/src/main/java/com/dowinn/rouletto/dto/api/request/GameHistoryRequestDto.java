package com.dowinn.rouletto.dto.api.request;

import lombok.Data;

@Data
public class GameHistoryRequestDto {
    private String from;
    private String to;
    private Long playerId;
    private String tableId;
}
