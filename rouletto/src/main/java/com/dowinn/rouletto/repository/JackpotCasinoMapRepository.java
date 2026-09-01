package com.dowinn.rouletto.repository;

import com.dowinn.rouletto.entity.JackpotCasinoMap;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JackpotCasinoMapRepository extends JpaRepository<JackpotCasinoMap,Long> {

    public JackpotCasinoMap findByCasinoId(String casino);
}
