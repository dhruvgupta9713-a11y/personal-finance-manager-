package com.finance.manager.repository;

import com.finance.manager.entity.Goal;
import com.finance.manager.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GoalRepository extends JpaRepository<Goal, Long> {

    // get all goals for a user
    List<Goal> findByUser(User user);
}
