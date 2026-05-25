package com.finance.manager.controller;

import com.finance.manager.dto.request.TransactionRequest;
import com.finance.manager.dto.response.MessageResponse;
import com.finance.manager.dto.response.TransactionResponse;
import com.finance.manager.service.TransactionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// handles creating, reading, updating and deleting transactions
@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    // create a new transaction (income or expense)
    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(
            @RequestBody @Valid TransactionRequest transactionRequest,
            HttpServletRequest request) {
        TransactionResponse created = transactionService.createTransaction(transactionRequest, request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    // get all transactions with optional filters
    // TODO: maybe add pagination later
    @GetMapping
    public ResponseEntity<Map<String, List<TransactionResponse>>> getAllTransactions(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long categoryId,
            HttpServletRequest request) {

        List<TransactionResponse> transactions = transactionService.getAllTransactions(
                startDate, endDate, categoryId, request);

        Map<String, List<TransactionResponse>> result = new HashMap<>();
        result.put("transactions", transactions);

        return ResponseEntity.ok(result);
    }

    // update an existing transaction by id
    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponse> updateTransaction(@PathVariable Long id,
                                                                  @RequestBody @Valid TransactionRequest transactionRequest,
                                                                  HttpServletRequest request) {
        TransactionResponse updated = transactionService.updateTransaction(id, transactionRequest, request);
        return ResponseEntity.ok(updated);
    }

    // delete a transaction
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteTransaction(@PathVariable Long id,
                                                             HttpServletRequest request) {
        MessageResponse response = transactionService.deleteTransaction(id, request);
        return ResponseEntity.ok(response);
    }
}
