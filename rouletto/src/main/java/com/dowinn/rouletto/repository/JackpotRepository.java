package com.dowinn.rouletto.repository;

import com.dowinn.rouletto.entity.Jackpot;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JackpotRepository extends JpaRepository<Jackpot,Long> {

    public Jackpot findByCasinoIdAndTableIdAndActiveTrue(String casinoId,String tableId);

    public List<Jackpot> findByCasinoIdInAndTableIdAndActiveTrue(List<String> casinoId,String tableId);


    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query(value = """
    UPDATE roulette.jackpot
    SET jackpotamount = jackpotamount + :betAmount
    WHERE jid = :id 
    """, nativeQuery = true)
    int updateJackpotAmount(@Param("id") Long id,
                            @Param("betAmount") Double betAmount);
}
