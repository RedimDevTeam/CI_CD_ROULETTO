package com.dowinn.rouletto.repository;

import com.dowinn.rouletto.entity.JackpotBet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JackpotBetRepository extends JpaRepository<JackpotBet,Long> {

    List<JackpotBet> findAllByGameId(String gameId);

    List<JackpotBet> findByGameIdAndPlayerId(String gameId,Long playerId);
}
