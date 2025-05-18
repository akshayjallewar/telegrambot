package com.splitwithease.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
public class Balance {

    @Id
    private Long userId;

    private Double amount;

    public Balance() {}

    public Balance(Long userId, Double amount) {
        this.userId = userId;
        this.amount = amount;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }
}
