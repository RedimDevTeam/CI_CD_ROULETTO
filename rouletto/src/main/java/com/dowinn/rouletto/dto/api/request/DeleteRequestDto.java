package com.dowinn.rouletto.dto.api.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeleteRequestDto {

    private Integer order;
    private String tableId;
}
