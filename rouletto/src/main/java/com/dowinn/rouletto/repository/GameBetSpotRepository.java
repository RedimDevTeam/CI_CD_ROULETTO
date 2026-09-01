package com.dowinn.rouletto.repository;

import com.dowinn.rouletto.entity.GameBetSpot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GameBetSpotRepository extends JpaRepository<GameBetSpot,Long> {

    List<GameBetSpot> findBytableIdAndCasinoIdAndCurrencyAndBetLimitTypeIdAndSpot_Active(String tableId,String casinoId,String currency,Integer betlimitypeId,Boolean isSpot);
}
