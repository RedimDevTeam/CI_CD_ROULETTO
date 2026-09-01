package com.dowinn.rouletto.dto.api.request;

import lombok.Data;

@Data
public class GameStartRequestDto {
    public String tableId;
    public String machineGameId;
    public String machineId;
}
