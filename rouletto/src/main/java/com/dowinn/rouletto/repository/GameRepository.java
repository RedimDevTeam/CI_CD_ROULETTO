package com.dowinn.rouletto.repository;

import com.dowinn.rouletto.entity.Game;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GameRepository extends JpaRepository<Game,String> {

    Optional<Game> findByStatusAndTableId(int status,String tableId);
    Optional<Game> findByStatusInAndTableId(List<Integer> status, String tableId);
}
