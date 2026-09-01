package com.dowinn.rouletto.dto.api.request;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SaveBetRequestDto {
    private String tableId;
    private Map<Integer,Double> mainBets;
    private Map<Integer,Double> sideBets;
    private List<List<Integer>> jackPotNumbers;
    private Integer id;


}
