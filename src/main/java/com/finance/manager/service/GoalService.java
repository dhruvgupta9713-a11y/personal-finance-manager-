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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GoalService {

    private final GoalRepository goalRepository;
    private final TransactionRepository transactionRepository;
    private final AuthService authService;

    public GoalService(GoalRepository goalRepository,
                       TransactionRepository transactionRepository,
                       AuthService authService) {
        this.goalRepository = goalRepository;
        this.transactionRepository = transactionRepository;
        this.authService = authService;
    }

    // create a new savings goal
    public GoalResponse createGoal(GoalRequest request, HttpServletRequest httpRequest) {
        User currentUser = authService.getCurrentUser(httpRequest);

        // if no start date provided, use today
        LocalDate startDate = request.getStartDate() != null ? request.getStartDate() : LocalDate.now();

        Goal goal = Goal.builder()
            .goalName(request.getGoalName())
            .targetAmount(request.getTargetAmount())
            .targetDate(request.getTargetDate())
            .startDate(startDate)
            .user(currentUser)
            .build();

        Goal saved = goalRepository.save(goal);
        return buildGoalResponse(saved, currentUser);
    }

    // get all goals for current user
    public List<GoalResponse> getAllGoals(HttpServletRequest httpRequest) {
        User currentUser = authService.getCurrentUser(httpRequest);

        List<Goal> goals = goalRepository.findByUser(currentUser);

        return goals.stream()
            .map(goal -> buildGoalResponse(goal, currentUser))
            .collect(Collectors.toList());
    }

    // get a specific goal by id
    public GoalResponse getGoalById(Long id, HttpServletRequest httpRequest) {
        User currentUser = authService.getCurrentUser(httpRequest);

        Goal goal = goalRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Goal not found"));

        // check if this goal belongs to the user
        if (!goal.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Not your goal");
        }

        return buildGoalResponse(goal, currentUser);
    }

    // update an existing goal
    public GoalResponse updateGoal(Long id, GoalRequest request, HttpServletRequest httpRequest) {
        User currentUser = authService.getCurrentUser(httpRequest);

        Goal existingGoal = goalRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Goal not found"));

        if (!existingGoal.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Not your goal");
        }

        // update the fields that were provided
        if (request.getGoalName() != null) {
            existingGoal.setGoalName(request.getGoalName());
        }
        if (request.getTargetAmount() != null) {
            existingGoal.setTargetAmount(request.getTargetAmount());
        }
        if (request.getTargetDate() != null) {
            existingGoal.setTargetDate(request.getTargetDate());
        }

        Goal updated = goalRepository.save(existingGoal);
        return buildGoalResponse(updated, currentUser);
    }

    // delete a goal
    public MessageResponse deleteGoal(Long id, HttpServletRequest httpRequest) {
        User currentUser = authService.getCurrentUser(httpRequest);

        Goal goal = goalRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Goal not found"));

        if (!goal.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Not your goal");
        }

        goalRepository.delete(goal);
        return new MessageResponse("Goal deleted successfully");
    }

    // calculate progress and build the goal response
    // TODO: can improve this later - maybe cache the calculation
    private GoalResponse buildGoalResponse(Goal goal, User user) {
        // get all transactions from the goal start date onwards
        List<Transaction> transactions = transactionRepository
            .findByUserAndDateGreaterThanEqual(user, goal.getStartDate());

        // calculate totals
        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpenses = BigDecimal.ZERO;

        for (Transaction t : transactions) {
            if (t.getCategory().getType() == Category.CategoryType.INCOME) {
                totalIncome = totalIncome.add(t.getAmount());
            } else {
                totalExpenses = totalExpenses.add(t.getAmount());
            }
        }

        // current progress is income minus expenses
        BigDecimal currentProgress = totalIncome.subtract(totalExpenses);
        if (currentProgress.compareTo(BigDecimal.ZERO) < 0) {
            currentProgress = BigDecimal.ZERO;
        }

        // calculate percentage
        BigDecimal progressPercentage = new BigDecimal("0.0");
        if (goal.getTargetAmount().compareTo(BigDecimal.ZERO) > 0) {
            progressPercentage = currentProgress
                .multiply(new BigDecimal("100"))
                .divide(goal.getTargetAmount(), 1, RoundingMode.HALF_UP);
        }

        // remaining amount
        BigDecimal remainingAmount = goal.getTargetAmount().subtract(currentProgress);
        if (remainingAmount.compareTo(BigDecimal.ZERO) < 0) {
            remainingAmount = BigDecimal.ZERO;
        }

        return GoalResponse.builder()
            .id(goal.getId())
            .goalName(goal.getGoalName())
            .targetAmount(goal.getTargetAmount())
            .targetDate(goal.getTargetDate())
            .startDate(goal.getStartDate())
            .currentProgress(currentProgress)
            .progressPercentage(progressPercentage)
            .remainingAmount(remainingAmount)
            .build();
    }
}
