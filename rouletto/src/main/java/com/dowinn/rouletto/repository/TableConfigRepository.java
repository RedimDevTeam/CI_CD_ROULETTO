package com.dowinn.rouletto.repository;

import com.dowinn.rouletto.entity.TableConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TableConfigRepository extends JpaRepository<TableConfig,Integer> {
    TableConfig findByTableIdAndActive(String tableId,boolean status);
}
