package com.finance.manager.service;

import com.finance.manager.dto.request.GoalRequest;
import com.finance.manager.dto.response.GoalResponse;
import com.finance.manager.dto.response.MessageResponse;
import com.finance.manager.entity.Category;
import com.finance.manager.entity.Goal;
import com.finance.manager.entity.Transaction;
import com.finance.manager.entity.User;
import com.finance.manager.exception.ResourceNotFoundException;
import com.finance.manager.repository.GoalRepository;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

// goal service tests
@ExtendWith(MockitoExtension.class)
class GoalServiceTest {

    @Mock
    private GoalRepository goalRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AuthService authService;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private GoalService goalService;

    private User testUser;
    private User otherUser;
    private Goal testGoal;
    private Category incomeCategory;
    private Category expenseCategory;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser@example.com");

        otherUser = new User();
        otherUser.setId(2L);
        otherUser.setUsername("someone_else@example.com");

        testGoal = Goal.builder()
                .id(1L)
                .goalName("Emergency Fund")
                .targetAmount(new BigDecimal("10000.00"))
                .targetDate(LocalDate.of(2027, 12, 31))
                .startDate(LocalDate.of(2026, 1, 1))
                .user(testUser)
                .build();

        incomeCategory = Category.builder()
                .id(1L).name("Salary").type(Category.CategoryType.INCOME).build();
        expenseCategory = Category.builder()
                .id(2L).name("Food").type(Category.CategoryType.EXPENSE).build();
    }

    @Test
    void testCreateGoalSuccess() {
        when(authService.getCurrentUser(request)).thenReturn(testUser);
        when(goalRepository.save(any(Goal.class))).thenReturn(testGoal);
        // for progress calc - no transactions yet
        when(transactionRepository.findByUserAndDateGreaterThanEqual(eq(testUser), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        GoalRequest goalRequest = new GoalRequest();
        goalRequest.setGoalName("Emergency Fund");
        goalRequest.setTargetAmount(new BigDecimal("10000.00"));
        goalRequest.setTargetDate(LocalDate.of(2027, 12, 31));
        goalRequest.setStartDate(LocalDate.of(2026, 1, 1));

        GoalResponse response = goalService.createGoal(goalRequest, request);

        assertNotNull(response);
        assertEquals("Emergency Fund", response.getGoalName());
        assertEquals(new BigDecimal("10000.00"), response.getTargetAmount());
        verify(goalRepository).save(any(Goal.class));
    }

    @Test
    void testCreateGoalDefaultStartDate() {
        // if start date is null, it should default to today
        when(authService.getCurrentUser(request)).thenReturn(testUser);

        Goal savedGoal = Goal.builder()
                .id(2L)
                .goalName("Vacation Fund")
                .targetAmount(new BigDecimal("5000"))
                .targetDate(LocalDate.of(2027, 12, 31))
                .startDate(LocalDate.now()) // should be set to today
                .user(testUser)
                .build();

        when(goalRepository.save(any(Goal.class))).thenReturn(savedGoal);
        when(transactionRepository.findByUserAndDateGreaterThanEqual(eq(testUser), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        GoalRequest goalRequest = new GoalRequest();
        goalRequest.setGoalName("Vacation Fund");
        goalRequest.setTargetAmount(new BigDecimal("5000"));
        goalRequest.setTargetDate(LocalDate.of(2027, 12, 31));
        goalRequest.setStartDate(null); // no start date

        GoalResponse response = goalService.createGoal(goalRequest, request);

        assertNotNull(response);
        // verify that the goal was saved
        verify(goalRepository).save(any(Goal.class));
    }

    @Test
    void testGetAllGoals() {
        when(authService.getCurrentUser(request)).thenReturn(testUser);

        Goal goal1 = Goal.builder()
                .id(1L).goalName("Emergency Fund").targetAmount(new BigDecimal("10000"))
                .targetDate(LocalDate.of(2027, 12, 31)).startDate(LocalDate.of(2026, 1, 1))
                .user(testUser).build();
        Goal goal2 = Goal.builder()
                .id(2L).goalName("New Car").targetAmount(new BigDecimal("25000"))
                .targetDate(LocalDate.of(2028, 6, 30)).startDate(LocalDate.of(2026, 3, 1))
                .user(testUser).build();

        when(goalRepository.findByUser(testUser)).thenReturn(Arrays.asList(goal1, goal2));
        // mock transaction repo for progress calculations
        when(transactionRepository.findByUserAndDateGreaterThanEqual(eq(testUser), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        List<GoalResponse> result = goalService.getAllGoals(request);

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void testGetGoalByIdSuccess() {
        when(authService.getCurrentUser(request)).thenReturn(testUser);
        when(goalRepository.findById(1L)).thenReturn(Optional.of(testGoal));

        // mock some transactions for progress calc
        Transaction incomeTransaction = Transaction.builder()
                .id(1L).amount(new BigDecimal("3000")).date(LocalDate.of(2026, 2, 1))
                .category(incomeCategory).user(testUser).build();
        Transaction expenseTransaction = Transaction.builder()
                .id(2L).amount(new BigDecimal("1000")).date(LocalDate.of(2026, 2, 5))
                .category(expenseCategory).user(testUser).build();

        when(transactionRepository.findByUserAndDateGreaterThanEqual(testUser, testGoal.getStartDate()))
                .thenReturn(Arrays.asList(incomeTransaction, expenseTransaction));

        GoalResponse response = goalService.getGoalById(1L, request);

        assertNotNull(response);
        assertEquals("Emergency Fund", response.getGoalName());
        assertEquals(new BigDecimal("10000.00"), response.getTargetAmount());
        // progress should be income - expenses = 3000 - 1000 = 2000
        assertEquals(0, new BigDecimal("2000").compareTo(response.getCurrentProgress()));
        assertEquals(0, new BigDecimal("8000").compareTo(response.getRemainingAmount()));
    }

    @Test
    void testGetGoalByIdNotOwner() {
        // goal belongs to someone else
        Goal otherUsersGoal = Goal.builder()
                .id(5L)
                .goalName("Not Your Goal")
                .targetAmount(new BigDecimal("1000"))
                .targetDate(LocalDate.of(2027, 12, 31))
                .startDate(LocalDate.of(2026, 1, 1))
                .user(otherUser)
                .build();

        when(authService.getCurrentUser(request)).thenReturn(testUser);
        when(goalRepository.findById(5L)).thenReturn(Optional.of(otherUsersGoal));

        // should throw because it's not testUser's goal
        assertThrows(AccessDeniedException.class, () -> {
            goalService.getGoalById(5L, request);
        });
    }

    @Test
    void testGetGoalByIdNotFound() {
        when(authService.getCurrentUser(request)).thenReturn(testUser);
        when(goalRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            goalService.getGoalById(99L, request);
        });
    }

    @Test
    void testUpdateGoalSuccess() {
        when(authService.getCurrentUser(request)).thenReturn(testUser);
        when(goalRepository.findById(1L)).thenReturn(Optional.of(testGoal));

        Goal updatedGoal = Goal.builder()
                .id(1L)
                .goalName("Updated Emergency Fund")
                .targetAmount(new BigDecimal("15000"))
                .targetDate(LocalDate.of(2028, 6, 30))
                .startDate(LocalDate.of(2026, 1, 1))
                .user(testUser)
                .build();
        when(goalRepository.save(any(Goal.class))).thenReturn(updatedGoal);
        when(transactionRepository.findByUserAndDateGreaterThanEqual(eq(testUser), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        GoalRequest updateRequest = new GoalRequest();
        updateRequest.setGoalName("Updated Emergency Fund");
        updateRequest.setTargetAmount(new BigDecimal("15000"));
        updateRequest.setTargetDate(LocalDate.of(2028, 6, 30));

        GoalResponse response = goalService.updateGoal(1L, updateRequest, request);

        assertNotNull(response);
        assertEquals("Updated Emergency Fund", response.getGoalName());
        verify(goalRepository).save(any(Goal.class));
    }

    @Test
    void testDeleteGoalSuccess() {
        when(authService.getCurrentUser(request)).thenReturn(testUser);
        when(goalRepository.findById(1L)).thenReturn(Optional.of(testGoal));

        MessageResponse response = goalService.deleteGoal(1L, request);

        assertNotNull(response);
        assertEquals("Goal deleted successfully", response.getMessage());
        verify(goalRepository).delete(testGoal);
    }
}
