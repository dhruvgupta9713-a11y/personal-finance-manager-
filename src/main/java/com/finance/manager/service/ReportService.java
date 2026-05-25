package com.finance.manager.service;

import com.finance.manager.dto.response.ReportResponse;
import com.finance.manager.entity.Category;
import com.finance.manager.entity.Transaction;
import com.finance.manager.entity.User;
import com.finance.manager.repository.TransactionRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportService {

    private final TransactionRepository transactionRepository;
    private final AuthService authService;

    public ReportService(TransactionRepository transactionRepository, AuthService authService) {
        this.transactionRepository = transactionRepository;
        this.authService = authService;
    }

    // get monthly report for a specific year and month
    // TODO: could optimize this query
    public ReportResponse getMonthlyReport(int year, int month, HttpServletRequest httpRequest) {
        User currentUser = authService.getCurrentUser(httpRequest);

        // figure out start and end of the month
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<Transaction> transactions = transactionRepository
            .findByUserAndDateBetweenOrderByDateDesc(currentUser, startDate, endDate);

        return buildReport(transactions, month, year);
    }

    // get yearly report for a full year
    public ReportResponse getYearlyReport(int year, HttpServletRequest httpRequest) {
        User currentUser = authService.getCurrentUser(httpRequest);

        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.of(year, 12, 31);

        List<Transaction> transactions = transactionRepository
            .findByUserAndDateBetweenOrderByDateDesc(currentUser, startDate, endDate);

        // month is null for yearly report
        return buildReport(transactions, null, year);
    }

    // helper to build the report from a list of transactions
    private ReportResponse buildReport(List<Transaction> transactions, Integer month, int year) {
        Map<String, BigDecimal> incomeByCategory = new HashMap<>();
        Map<String, BigDecimal> expenseByCategory = new HashMap<>();
        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpenses = BigDecimal.ZERO;

        for (Transaction t : transactions) {
            String categoryName = t.getCategory().getName();
            BigDecimal amount = t.getAmount();

            if (t.getCategory().getType() == Category.CategoryType.INCOME) {
                // add to income map
                incomeByCategory.merge(categoryName, amount, BigDecimal::add);
                totalIncome = totalIncome.add(amount);
            } else {
                // add to expense map
                expenseByCategory.merge(categoryName, amount, BigDecimal::add);
                totalExpenses = totalExpenses.add(amount);
            }
        }

        BigDecimal netSavings = totalIncome.subtract(totalExpenses);

        return ReportResponse.builder()
            .month(month)
            .year(year)
            .totalIncome(incomeByCategory)
            .totalExpenses(expenseByCategory)
            .netSavings(netSavings)
            .build();
    }
}
