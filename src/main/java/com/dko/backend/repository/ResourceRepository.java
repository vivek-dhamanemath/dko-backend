package com.dko.backend.repository;

import com.dko.backend.model.Resource;
import com.dko.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ResourceRepository extends JpaRepository<Resource, UUID>,
        JpaSpecificationExecutor<Resource> {

    List<Resource> findByUserOrderByCreatedAtDesc(User user);

    Optional<Resource> findByIdAndUser(UUID id, User user);
}
