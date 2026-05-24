package com.finance.manager.repository;

import com.finance.manager.entity.Category;
import com.finance.manager.entity.Transaction;
import com.finance.manager.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // get all transactions for a user, newest first
    List<Transaction> findByUserOrderByDateDesc(User user);

    // filter by date range
    List<Transaction> findByUserAndDateBetweenOrderByDateDesc(User user, LocalDate start, LocalDate end);

    // filter by category
    List<Transaction> findByUserAndCategoryOrderByDateDesc(User user, Category category);

    // filter by both date range and category
    List<Transaction> findByUserAndDateBetweenAndCategoryOrderByDateDesc(
            User user, LocalDate start, LocalDate end, Category category);

    // check if any transactions exist for this category (useful before deleting a category)
    boolean existsByCategoryAndUser(Category category, User user);

    // TODO: might want to add pagination later for large datasets
    // get transactions from a specific start date onwards
    @Query("SELECT t FROM Transaction t WHERE t.user = :user AND t.date >= :startDate")
    List<Transaction> findByUserAndDateGreaterThanEqual(@Param("user") User user, @Param("startDate") LocalDate startDate);
}
