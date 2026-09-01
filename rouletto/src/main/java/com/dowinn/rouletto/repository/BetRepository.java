package com.dowinn.rouletto.repository;

import com.dowinn.rouletto.entity.Bets;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

public interface BetRepository extends JpaRepository<Bets,Long> {

    public List<Bets> findByGameId(String gameId);
    public List<Bets> findByGameIdAndPlayerId(String gameId,Long playerId);
}
