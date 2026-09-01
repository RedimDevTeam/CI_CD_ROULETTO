package com.dowinn.rouletto.dto.api.request;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GameCancelRequestDto {

    private String gameId;
    private String tableId;
    private String reason;
}
