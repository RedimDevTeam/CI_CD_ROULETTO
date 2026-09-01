package com.dowinn.rouletto.repository;

import com.dowinn.rouletto.entity.Shoes;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShoeRepository extends JpaRepository<Shoes,Long> {

    Optional<Shoes> findByTableIdAndActiveTrue(String tableId);
}


