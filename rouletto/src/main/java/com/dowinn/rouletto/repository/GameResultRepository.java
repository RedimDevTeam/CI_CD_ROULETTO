package com.dowinn.rouletto.repository;

import com.dowinn.rouletto.entity.GameResults;
import com.dowinn.rouletto.model.GameResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface GameResultRepository extends JpaRepository<GameResults,Long> {


 /*   @Query(value ="select rg.* from roulette.gameresults rg" +
            " right join roulette.shoe rs " +
            "on rg.shoeid=rs.id " +
            " where rs.active=true and tableId=?1" +
            "order by rg.id desc"+
            "   limit ?2",nativeQuery = true )
    List<GameResults> findAllByTableId(String tableId,Integer limit);*/

    @Query(value = "SELECT * FROM roulette.gameresults rg where " +
            " rg.tableid = ?1 " +
            "ORDER BY rg.id DESC " +
            "LIMIT ?2",
            nativeQuery = true)
    List<GameResults> findAllByTableId(String tableId, Integer limit);

}
