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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final AuthService authService;

    public TransactionService(TransactionRepository transactionRepository,
                              CategoryRepository categoryRepository,
                              AuthService authService) {
        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
        this.authService = authService;
    }

    // create a new transaction
    public TransactionResponse createTransaction(TransactionRequest request, HttpServletRequest httpRequest) {
        User currentUser = authService.getCurrentUser(httpRequest);

        // find the category - check user's custom categories first, then default
        Category foundCategory = categoryRepository.findByNameAndUser(request.getCategory(), currentUser)
            .orElse(categoryRepository.findByNameAndUserIsNull(request.getCategory())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found")));

        Transaction transaction = Transaction.builder()
            .amount(request.getAmount())
            .date(request.getDate())
            .description(request.getDescription())
            .category(foundCategory)
            .user(currentUser)
            .build();

        Transaction saved = transactionRepository.save(transaction);
        return convertToResponse(saved);
    }

    // get all transactions with optional filters
    // TODO: maybe add pagination later
    public List<TransactionResponse> getAllTransactions(LocalDate startDate, LocalDate endDate,
                                                        Long categoryId, HttpServletRequest httpRequest) {
        User currentUser = authService.getCurrentUser(httpRequest);

        List<Transaction> transactions;

        if (startDate != null && endDate != null && categoryId != null) {
            // filter by date range and category
            Category cat = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            transactions = transactionRepository.findByUserAndDateBetweenAndCategoryOrderByDateDesc(
                currentUser, startDate, endDate, cat);

        } else if (startDate != null && endDate != null) {
            // filter by date range only
            transactions = transactionRepository.findByUserAndDateBetweenOrderByDateDesc(
                currentUser, startDate, endDate);

        } else if (categoryId != null) {
            // filter by category only
            Category cat = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            transactions = transactionRepository.findByUserAndCategoryOrderByDateDesc(currentUser, cat);

        } else {
            // no filters, get everything
            transactions = transactionRepository.findByUserOrderByDateDesc(currentUser);
        }

        return transactions.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }

    // update an existing transaction
    public TransactionResponse updateTransaction(Long id, TransactionRequest request,
                                                  HttpServletRequest httpRequest) {
        User currentUser = authService.getCurrentUser(httpRequest);

        Transaction existingTransaction = transactionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        // make sure this transaction belongs to the current user
        if (!existingTransaction.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Not your transaction");
        }

        // update fields - but NOT the date
        if (request.getAmount() != null) {
            existingTransaction.setAmount(request.getAmount());
        }
        if (request.getDescription() != null) {
            existingTransaction.setDescription(request.getDescription());
        }
        if (request.getCategory() != null) {
            Category newCategory = categoryRepository.findByNameAndUser(request.getCategory(), currentUser)
                .orElse(categoryRepository.findByNameAndUserIsNull(request.getCategory())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found")));
            existingTransaction.setCategory(newCategory);
        }

        Transaction updated = transactionRepository.save(existingTransaction);
        return convertToResponse(updated);
    }

    // delete a transaction
    public MessageResponse deleteTransaction(Long id, HttpServletRequest httpRequest) {
        User currentUser = authService.getCurrentUser(httpRequest);

        Transaction transaction = transactionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        if (!transaction.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Not your transaction");
        }

        transactionRepository.delete(transaction);
        return new MessageResponse("Transaction deleted successfully");
    }

    // helper to convert entity to response DTO
    private TransactionResponse convertToResponse(Transaction transaction) {
        return TransactionResponse.builder()
            .id(transaction.getId())
            .amount(transaction.getAmount())
            .date(transaction.getDate())
            .category(transaction.getCategory().getName())
            .description(transaction.getDescription())
            .type(transaction.getCategory().getType().name())
            .build();
    }
}
