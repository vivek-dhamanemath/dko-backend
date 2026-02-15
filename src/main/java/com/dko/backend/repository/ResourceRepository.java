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

        @org.springframework.data.jpa.repository.Query("SELECT r FROM Resource r WHERE r.user = :user AND r.isArchived = :isArchived AND r.isDeleted = false ORDER BY r.createdAt DESC")
        List<Resource> findByUserAndIsArchivedOrderByCreatedAtDesc(
                        @org.springframework.data.repository.query.Param("user") User user,
                        @org.springframework.data.repository.query.Param("isArchived") Boolean isArchived);

        Optional<Resource> findByIdAndUser(UUID id, User user);

        // Trash
        List<Resource> findByUserAndIsDeletedTrueOrderByDeletedAtDesc(User user);

        // Stats
        long countByUser(User user); // lifetime (all resources ever)

        long countByUserAndIsDeletedFalse(User user); // active (not deleted)

        long countByUserAndIsDeletedFalseAndIsArchivedTrue(User user); // archived

        long countByUserAndIsDeletedTrue(User user); // in trash
}
