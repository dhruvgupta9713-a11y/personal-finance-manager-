package com.finance.manager.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

// response for monthly/yearly financial report
@Data
@Builder
public class ReportResponse {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer month; // only included for monthly reports
    private Integer year;

    // category name -> total amount for income categories
    private Map<String, BigDecimal> totalIncome;

    // category name -> total amount for expense categories
    private Map<String, BigDecimal> totalExpenses;

    // income - expenses
    private BigDecimal netSavings;
}
