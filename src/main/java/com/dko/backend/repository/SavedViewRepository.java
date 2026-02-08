package com.dko.backend.repository;

import com.dko.backend.model.SavedView;
import com.dko.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SavedViewRepository extends JpaRepository<SavedView, UUID> {

    List<SavedView> findByUserOrderByCreatedAtDesc(User user);

    Optional<SavedView> findByIdAndUser(UUID id, User user);

    Optional<SavedView> findByNameAndUser(String name, User user);
}
