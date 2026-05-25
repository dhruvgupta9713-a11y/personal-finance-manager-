package com.finance.manager.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

// response for a single transaction
@Data
@Builder
public class TransactionResponse {

    private Long id;
    private BigDecimal amount;
    private LocalDate date;
    private String category;
    private String description;
    private String type; // INCOME or EXPENSE
}
