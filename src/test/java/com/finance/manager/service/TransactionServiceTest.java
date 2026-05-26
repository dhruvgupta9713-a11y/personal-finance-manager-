package com.finance.manager.service;

import com.finance.manager.dto.request.TransactionRequest;
import com.finance.manager.dto.response.MessageResponse;
import com.finance.manager.dto.response.TransactionResponse;
import com.finance.manager.entity.Category;
import com.finance.manager.entity.Transaction;
import com.finance.manager.entity.User;
import com.finance.manager.exception.ResourceNotFoundException;
import com.finance.manager.repository.CategoryRepository;
import com.finance.manager.repository.TransactionRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// tests for transaction service
@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private AuthService authService;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private TransactionService transactionService;

    private User testUser;
    private User otherUser;
    private Category salaryCategory;
    private Category foodCategory;
    private Transaction testTransaction;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser@example.com");

        otherUser = new User();
        otherUser.setId(2L);
        otherUser.setUsername("otheruser@example.com");

        salaryCategory = Category.builder()
                .id(1L)
                .name("Salary")
                .type(Category.CategoryType.INCOME)
                .isCustom(false)
                .build();

        foodCategory = Category.builder()
                .id(2L)
                .name("Food")
                .type(Category.CategoryType.EXPENSE)
                .isCustom(false)
                .build();

        testTransaction = Transaction.builder()
                .id(1L)
                .amount(new BigDecimal("5000.00"))
                .date(LocalDate.of(2026, 1, 15))
                .description("Monthly salary")
                .category(salaryCategory)
                .user(testUser)
                .build();
    }

    @Test
    void testCreateTransactionSuccess() {
        when(authService.getCurrentUser(request)).thenReturn(testUser);
        // need to find the category first - could be default or user's custom
        when(categoryRepository.findByNameAndUser("Salary", testUser))
                .thenReturn(Optional.empty());
        when(categoryRepository.findByNameAndUserIsNull("Salary"))
                .thenReturn(Optional.of(salaryCategory));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

        TransactionRequest transactionRequest = new TransactionRequest();
        transactionRequest.setAmount(new BigDecimal("5000.00"));
        transactionRequest.setDate(LocalDate.of(2026, 1, 15));
        transactionRequest.setCategory("Salary");
        transactionRequest.setDescription("Monthly salary");

        TransactionResponse response = transactionService.createTransaction(transactionRequest, request);

        assertNotNull(response);
        assertEquals(new BigDecimal("5000.00"), response.getAmount());
        assertEquals("Salary", response.getCategory());
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void testCreateTransactionCategoryNotFound() {
        when(authService.getCurrentUser(request)).thenReturn(testUser);
        when(categoryRepository.findByNameAndUser("FakeCategory", testUser))
                .thenReturn(Optional.empty());
        when(categoryRepository.findByNameAndUserIsNull("FakeCategory"))
                .thenReturn(Optional.empty());

        TransactionRequest transactionRequest = new TransactionRequest();
        transactionRequest.setAmount(new BigDecimal("100.00"));
        transactionRequest.setDate(LocalDate.now());
        transactionRequest.setCategory("FakeCategory");
        transactionRequest.setDescription("test");

        assertThrows(ResourceNotFoundException.class, () -> {
            transactionService.createTransaction(transactionRequest, request);
        });
    }

    @Test
    void testGetAllTransactionsNoFilters() {
        // no date range or category filter - just get everything
        when(authService.getCurrentUser(request)).thenReturn(testUser);

        Transaction t1 = Transaction.builder()
                .id(1L).amount(new BigDecimal("5000")).date(LocalDate.of(2026, 1, 15))
                .description("Salary").category(salaryCategory).user(testUser).build();
        Transaction t2 = Transaction.builder()
                .id(2L).amount(new BigDecimal("50")).date(LocalDate.of(2026, 1, 16))
                .description("Lunch").category(foodCategory).user(testUser).build();

        when(transactionRepository.findByUserOrderByDateDesc(testUser))
                .thenReturn(Arrays.asList(t1, t2));

        List<TransactionResponse> result = transactionService.getAllTransactions(null, null, null, request);

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void testGetAllTransactionsWithDateRange() {
        // filter by date range
        when(authService.getCurrentUser(request)).thenReturn(testUser);

        LocalDate startDate = LocalDate.of(2026, 1, 1);
        LocalDate endDate = LocalDate.of(2026, 1, 31);

        Transaction t1 = Transaction.builder()
                .id(1L).amount(new BigDecimal("5000")).date(LocalDate.of(2026, 1, 15))
                .description("Salary").category(salaryCategory).user(testUser).build();

        when(transactionRepository.findByUserAndDateBetweenOrderByDateDesc(testUser, startDate, endDate))
                .thenReturn(Arrays.asList(t1));

        List<TransactionResponse> result = transactionService.getAllTransactions(startDate, endDate, null, request);

        assertEquals(1, result.size());
    }

    @Test
    void testUpdateTransactionSuccess() {
        when(authService.getCurrentUser(request)).thenReturn(testUser);
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(testTransaction));
        when(categoryRepository.findByNameAndUser("Salary", testUser))
                .thenReturn(Optional.empty());
        when(categoryRepository.findByNameAndUserIsNull("Salary"))
                .thenReturn(Optional.of(salaryCategory));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);

        TransactionRequest updateRequest = new TransactionRequest();
        updateRequest.setAmount(new BigDecimal("5500.00"));
        updateRequest.setDate(LocalDate.of(2026, 1, 15));
        updateRequest.setCategory("Salary");
        updateRequest.setDescription("Updated salary");

        TransactionResponse response = transactionService.updateTransaction(1L, updateRequest, request);

        assertNotNull(response);
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void testUpdateTransactionNotOwner() {
        // someone elses transaction - should fail
        Transaction otherUsersTransaction = Transaction.builder()
                .id(5L)
                .amount(new BigDecimal("200"))
                .date(LocalDate.now())
                .description("not yours")
                .category(foodCategory)
                .user(otherUser)
                .build();

        when(authService.getCurrentUser(request)).thenReturn(testUser);
        when(transactionRepository.findById(5L)).thenReturn(Optional.of(otherUsersTransaction));

        TransactionRequest updateRequest = new TransactionRequest();
        updateRequest.setAmount(new BigDecimal("999.00"));
        updateRequest.setDate(LocalDate.now());
        updateRequest.setCategory("Food");
        updateRequest.setDescription("hacking attempt");

        // should throw access denied
        assertThrows(AccessDeniedException.class, () -> {
            transactionService.updateTransaction(5L, updateRequest, request);
        });
    }

    @Test
    void testDeleteTransactionSuccess() {
        when(authService.getCurrentUser(request)).thenReturn(testUser);
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(testTransaction));

        MessageResponse response = transactionService.deleteTransaction(1L, request);

        assertNotNull(response);
        assertEquals("Transaction deleted successfully", response.getMessage());
        verify(transactionRepository).delete(testTransaction);
    }

    @Test
    void testDeleteTransactionNotFound() {
        when(authService.getCurrentUser(request)).thenReturn(testUser);
        when(transactionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            transactionService.deleteTransaction(99L, request);
        });
    }
}
