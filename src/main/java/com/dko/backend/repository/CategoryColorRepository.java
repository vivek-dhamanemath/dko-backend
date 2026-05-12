package com.dko.backend.repository;

import com.dko.backend.model.CategoryColor;
import com.dko.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryColorRepository extends JpaRepository<CategoryColor, UUID> {
    List<CategoryColor> findByUser(User user);
    Optional<CategoryColor> findByUserAndCategoryName(User user, String categoryName);
}
