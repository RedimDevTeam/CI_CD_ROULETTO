package com.dowinn.rouletto.dto.socket.request;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RecordDto {

    private String gameId;
    private String tableId;
}
