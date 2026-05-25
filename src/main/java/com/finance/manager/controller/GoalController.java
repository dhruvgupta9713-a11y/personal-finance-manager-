package com.finance.manager.controller;

import com.finance.manager.dto.request.GoalRequest;
import com.finance.manager.dto.response.GoalResponse;
import com.finance.manager.dto.response.MessageResponse;
import com.finance.manager.service.GoalService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// controller for savings goals
@RestController
@RequestMapping("/api/goals")
public class GoalController {

    private final GoalService goalService;

    public GoalController(GoalService goalService) {
        this.goalService = goalService;
    }

    // create a new savings goal
    @PostMapping
    public ResponseEntity<GoalResponse> createGoal(@RequestBody @Valid GoalRequest goalRequest,
                                                    HttpServletRequest request) {
        GoalResponse newGoal = goalService.createGoal(goalRequest, request);
        return new ResponseEntity<>(newGoal, HttpStatus.CREATED);
    }

    // get all goals for the current user
    @GetMapping
    public ResponseEntity<Map<String, List<GoalResponse>>> getAllGoals(HttpServletRequest request) {
        List<GoalResponse> allGoals = goalService.getAllGoals(request);

        // TODO: can improve this later - maybe use a utility method for wrapping
        Map<String, List<GoalResponse>> result = new HashMap<>();
        result.put("goals", allGoals);

        return ResponseEntity.ok(result);
    }

    // get a single goal by its id
    @GetMapping("/{id}")
    public ResponseEntity<GoalResponse> getGoalById(@PathVariable Long id, HttpServletRequest request) {
        GoalResponse goal = goalService.getGoalById(id, request);
        return ResponseEntity.ok(goal);
    }

    // update a goal
    @PutMapping("/{id}")
    public ResponseEntity<GoalResponse> updateGoal(@PathVariable Long id,
                                                    @RequestBody @Valid GoalRequest goalRequest,
                                                    HttpServletRequest request) {
        GoalResponse updatedGoal = goalService.updateGoal(id, goalRequest, request);
        return ResponseEntity.ok(updatedGoal);
    }

    // delete a goal by id
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteGoal(@PathVariable Long id, HttpServletRequest request) {
        MessageResponse response = goalService.deleteGoal(id, request);
        return ResponseEntity.ok(response);
    }
}
