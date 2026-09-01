package com.dowinn.rouletto.repository;

import com.dowinn.rouletto.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction,Long> {
}
