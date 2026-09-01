package com.dowinn.rouletto.model;

import com.dowinn.rouletto.entity.Jackpot;
import com.dowinn.rouletto.entity.JackpotBet;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JackpotBetMap {

    private Jackpot jackpot;
    private List<JackpotBet> jackpotBetList;
}
