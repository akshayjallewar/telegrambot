package com.splitwithease.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.splitwithease.model.TransactionEntity;

public interface TransactionRepository extends JpaRepository<TransactionEntity, Long> {
	List<TransactionEntity> findByUserIdOrderByTimestampDesc(Long userId);
	}

