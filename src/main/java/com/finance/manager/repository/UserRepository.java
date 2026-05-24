package com.finance.manager.repository;

import com.finance.manager.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // find user by their email (username)
    Optional<User> findByUsername(String username);

    // check if email already registered
    boolean existsByUsername(String username);
}
