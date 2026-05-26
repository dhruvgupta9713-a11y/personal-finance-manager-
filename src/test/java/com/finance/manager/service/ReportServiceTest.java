package com.finance.manager.service;

import com.finance.manager.dto.response.ReportResponse;
import com.finance.manager.entity.Category;
import com.finance.manager.entity.Transaction;
import com.finance.manager.entity.User;
import com.finance.manager.repository.TransactionRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

// report service tests - checking monthly and yearly reports
@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AuthService authService;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private ReportService reportService;

    private User testUser;
    private Category salaryCategory;
    private Category freelanceCategory;
    private Category foodCategory;
    private Category rentCategory;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser@example.com");

        salaryCategory = Category.builder()
                .id(1L).name("Salary").type(Category.CategoryType.INCOME).build();
        freelanceCategory = Category.builder()
                .id(2L).name("Freelance").type(Category.CategoryType.INCOME).build();
        foodCategory = Category.builder()
                .id(3L).name("Food").type(Category.CategoryType.EXPENSE).build();
        rentCategory = Category.builder()
                .id(4L).name("Rent").type(Category.CategoryType.EXPENSE).build();
    }

    @Test
    void testMonthlyReport() {
        // January 2026 report with some income and expenses
        when(authService.getCurrentUser(request)).thenReturn(testUser);

        List<Transaction> januaryTransactions = new ArrayList<>();

        // income
        januaryTransactions.add(Transaction.builder()
                .id(1L).amount(new BigDecimal("5000.00")).date(LocalDate.of(2026, 1, 1))
                .description("Monthly salary").category(salaryCategory).user(testUser).build());
        januaryTransactions.add(Transaction.builder()
                .id(2L).amount(new BigDecimal("1500.00")).date(LocalDate.of(2026, 1, 10))
                .description("Side project").category(freelanceCategory).user(testUser).build());

        // expenses
        januaryTransactions.add(Transaction.builder()
                .id(3L).amount(new BigDecimal("200.00")).date(LocalDate.of(2026, 1, 5))
                .description("Groceries").category(foodCategory).user(testUser).build());
        januaryTransactions.add(Transaction.builder()
                .id(4L).amount(new BigDecimal("1200.00")).date(LocalDate.of(2026, 1, 1))
                .description("Monthly rent").category(rentCategory).user(testUser).build());

        LocalDate startOfMonth = LocalDate.of(2026, 1, 1);
        LocalDate endOfMonth = LocalDate.of(2026, 1, 31);

        when(transactionRepository.findByUserAndDateBetweenOrderByDateDesc(
                eq(testUser), eq(startOfMonth), eq(endOfMonth)))
                .thenReturn(januaryTransactions);

        ReportResponse report = reportService.getMonthlyReport(2026, 1, request);

        assertNotNull(report);
        assertEquals(1, report.getMonth());
        assertEquals(2026, report.getYear());

        // check income map
        assertNotNull(report.getTotalIncome());
        assertEquals(0, new BigDecimal("5000.00").compareTo(report.getTotalIncome().get("Salary")));
        assertEquals(0, new BigDecimal("1500.00").compareTo(report.getTotalIncome().get("Freelance")));

        // check expense map
        assertNotNull(report.getTotalExpenses());
        assertEquals(0, new BigDecimal("200.00").compareTo(report.getTotalExpenses().get("Food")));
        assertEquals(0, new BigDecimal("1200.00").compareTo(report.getTotalExpenses().get("Rent")));

        // net savings = 6500 - 1400 = 5100
        assertEquals(0, new BigDecimal("5100.00").compareTo(report.getNetSavings()),
                "Net savings should be 5100.00");
    }

    @Test
    void testMonthlyReportNoTransactions() {
        // month with zero transactions
        when(authService.getCurrentUser(request)).thenReturn(testUser);

        LocalDate startOfMonth = LocalDate.of(2026, 3, 1);
        LocalDate endOfMonth = LocalDate.of(2026, 3, 31);

        when(transactionRepository.findByUserAndDateBetweenOrderByDateDesc(
                eq(testUser), eq(startOfMonth), eq(endOfMonth)))
                .thenReturn(Collections.emptyList());

        ReportResponse report = reportService.getMonthlyReport(2026, 3, request);

        assertNotNull(report);
        assertEquals(3, report.getMonth());
        assertEquals(2026, report.getYear());

        // everything should be zero/empty
        assertTrue(report.getTotalIncome().isEmpty());
        assertTrue(report.getTotalExpenses().isEmpty());
        assertEquals(0, BigDecimal.ZERO.compareTo(report.getNetSavings()),
                "No transactions means zero net savings");
    }

    @Test
    void testYearlyReport() {
        // full year 2026 report
        when(authService.getCurrentUser(request)).thenReturn(testUser);

        List<Transaction> yearlyTransactions = new ArrayList<>();

        // income transactions
        yearlyTransactions.add(Transaction.builder()
                .id(1L).amount(new BigDecimal("5000.00")).date(LocalDate.of(2026, 1, 1))
                .description("Jan salary").category(salaryCategory).user(testUser).build());
        yearlyTransactions.add(Transaction.builder()
                .id(2L).amount(new BigDecimal("5000.00")).date(LocalDate.of(2026, 2, 1))
                .description("Feb salary").category(salaryCategory).user(testUser).build());
        yearlyTransactions.add(Transaction.builder()
                .id(3L).amount(new BigDecimal("2000.00")).date(LocalDate.of(2026, 3, 15))
                .description("Freelance gig").category(freelanceCategory).user(testUser).build());

        // expense transactions
        yearlyTransactions.add(Transaction.builder()
                .id(4L).amount(new BigDecimal("1200.00")).date(LocalDate.of(2026, 1, 1))
                .description("Jan rent").category(rentCategory).user(testUser).build());
        yearlyTransactions.add(Transaction.builder()
                .id(5L).amount(new BigDecimal("1200.00")).date(LocalDate.of(2026, 2, 1))
                .description("Feb rent").category(rentCategory).user(testUser).build());
        yearlyTransactions.add(Transaction.builder()
                .id(6L).amount(new BigDecimal("500.00")).date(LocalDate.of(2026, 1, 15))
                .description("Food stuff").category(foodCategory).user(testUser).build());

        LocalDate startOfYear = LocalDate.of(2026, 1, 1);
        LocalDate endOfYear = LocalDate.of(2026, 12, 31);

        when(transactionRepository.findByUserAndDateBetweenOrderByDateDesc(
                eq(testUser), eq(startOfYear), eq(endOfYear)))
                .thenReturn(yearlyTransactions);

        ReportResponse report = reportService.getYearlyReport(2026, request);

        assertNotNull(report);
        assertEquals(2026, report.getYear());
        assertNull(report.getMonth(), "Yearly report should have null month");

        // check income - Salary: 10000, Freelance: 2000
        assertNotNull(report.getTotalIncome());
        assertEquals(0, new BigDecimal("10000.00").compareTo(report.getTotalIncome().get("Salary")));
        assertEquals(0, new BigDecimal("2000.00").compareTo(report.getTotalIncome().get("Freelance")));

        // check expenses - Rent: 2400, Food: 500
        assertNotNull(report.getTotalExpenses());
        assertEquals(0, new BigDecimal("2400.00").compareTo(report.getTotalExpenses().get("Rent")));
        assertEquals(0, new BigDecimal("500.00").compareTo(report.getTotalExpenses().get("Food")));

        // net: 12000 - 2900 = 9100
        BigDecimal expectedNet = new BigDecimal("9100.00");
        assertEquals(0, expectedNet.compareTo(report.getNetSavings()),
                "Yearly net savings should be 9100");
    }
}
