package com.dko.backend.repository;

import com.dko.backend.model.Tag;
import com.dko.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TagRepository extends JpaRepository<Tag, UUID> {

    Optional<Tag> findByNameAndUser(String name, User user);
}
