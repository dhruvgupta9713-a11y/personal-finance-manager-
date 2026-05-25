package com.finance.manager.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

// response for a savings goal with progress info
@Data
@Builder
public class GoalResponse {

    private Long id;
    private String goalName;
    private BigDecimal targetAmount;
    private LocalDate targetDate;
    private LocalDate startDate;
    private BigDecimal currentProgress;
    private double progressPercentage;
    private BigDecimal remainingAmount;
}
