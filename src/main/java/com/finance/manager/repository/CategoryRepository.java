package com.finance.manager.repository;

import com.finance.manager.entity.Category;
import com.finance.manager.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // gets all default categories (user is null) plus the user's own custom ones
    List<Category> findByUserIsNullOrUser(User user);

    // find a default category by name
    Optional<Category> findByNameAndUserIsNull(String name);

    // find a users custom category by name
    Optional<Category> findByNameAndUser(String name, User user);

    // check if default category exists with this name
    boolean existsByNameAndUserIsNull(String name);

    // check if user already has a custom category with this name
    boolean existsByNameAndUser(String name, User user);
}
