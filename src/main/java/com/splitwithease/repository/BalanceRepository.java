package com.splitwithease.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.splitwithease.model.Balance;

public interface BalanceRepository extends  JpaRepository<Balance, Long>{

}
