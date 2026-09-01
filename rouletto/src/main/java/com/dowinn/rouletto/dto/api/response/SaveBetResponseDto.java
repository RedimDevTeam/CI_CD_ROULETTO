package com.dowinn.rouletto.dto.api.response;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SaveBetResponseDto {

    private Object betDetails;
    private Integer id;
}
