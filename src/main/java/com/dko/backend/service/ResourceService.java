package com.dko.backend.service;

import com.dko.backend.dto.FilterCriteria;
import com.dko.backend.model.Resource;
import com.dko.backend.model.ResourceTag;
import com.dko.backend.model.Tag;
import com.dko.backend.model.User;
import com.dko.backend.repository.ResourceRepository;
import com.dko.backend.repository.ResourceSpecifications;
import com.dko.backend.repository.ResourceTagRepository;
import com.dko.backend.repository.TagRepository;
import com.dko.backend.repository.UserRepository;
import com.dko.backend.dto.CreateResourceRequest;
import com.dko.backend.dto.UpdateResourceRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ResourceService {

    private final ResourceRepository resourceRepository;
    private final TagRepository tagRepository;
    private final ResourceTagRepository resourceTagRepository;
    private final UserRepository userRepository;

    @Transactional
    public Resource create(CreateResourceRequest request, User user) {
        Resource resource = new Resource();
        resource.setUser(user);
        resource.setUrl(request.getUrl());
        resource.setTitle(request.getTitle());
        resource.setNote(request.getNote());
        resource.setCategory(request.getCategory());

        Resource saved = resourceRepository.save(resource);

        // Handle tags
        if (request.getTags() != null) {
            for (String tagName : request.getTags()) {
                Tag tag = tagRepository.findByNameAndUser(tagName, user)
                        .orElseGet(() -> {
                            Tag newTag = new Tag();
                            newTag.setName(tagName);
                            newTag.setUser(user);
                            return tagRepository.save(newTag);
                        });

                ResourceTag resourceTag = new ResourceTag();
                resourceTag.setResource(saved);
                resourceTag.setTag(tag);
                resourceTagRepository.save(resourceTag);
            }
        }

        // Increment Persistent Lifetime Counter
        User currentUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        currentUser.setLifetimeResourcesCount(currentUser.getLifetimeResourcesCount() + 1);
        userRepository.save(currentUser);

        return saved;
    }

    public List<Resource> getUserResources(User user, boolean isArchived) {
        return resourceRepository.findByUserAndIsArchivedOrderByCreatedAtDesc(user, isArchived);
    }

    public List<Resource> getFilteredResources(User user, FilterCriteria criteria) {
        // Start with user-scoped specification
        Specification<Resource> spec = Specification.where(
                ResourceSpecifications.belongsToUser(user));

        // Add category filter
        if (criteria.getCategories() != null && !criteria.getCategories().isEmpty()) {
            spec = spec.and(ResourceSpecifications.hasCategories(criteria.getCategories()));
        }

        // Add tag filter
        if (criteria.getTags() != null && !criteria.getTags().isEmpty()) {
            spec = spec.and(ResourceSpecifications.hasTags(criteria.getTags()));
        }

        // Handle date range
        LocalDate start = null, end = null;
        if ("last_7_days".equals(criteria.getDateRange())) {
            end = LocalDate.now();
            start = end.minusDays(7);
        } else if ("last_30_days".equals(criteria.getDateRange())) {
            end = LocalDate.now();
            start = end.minusDays(30);
        } else if ("last_90_days".equals(criteria.getDateRange())) {
            end = LocalDate.now();
            start = end.minusDays(90);
        } else if ("custom".equals(criteria.getDateRange())) {
            start = criteria.getStartDate();
            end = criteria.getEndDate();
        }

        if (start != null && end != null) {
            spec = spec.and(ResourceSpecifications.createdBetween(start, end));
        }

        if (criteria.getCollectionId() != null) {
            spec = spec.and(ResourceSpecifications.isInCollection(criteria.getCollectionId()));
        }

        // Handle archive status (default to false if null)
        boolean includeArchived = criteria.getIsArchived() != null ? criteria.getIsArchived() : false;
        spec = spec.and(ResourceSpecifications.isArchived(includeArchived));

        // Always exclude soft-deleted resources from normal queries
        spec = spec.and(ResourceSpecifications.isNotDeleted());

        // Execute query with sorting
        return resourceRepository.findAll(spec,
                Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    public void delete(UUID id, User user) {
        Resource resource = resourceRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new RuntimeException("Resource not found"));

        resource.setIsDeleted(true);
        resource.setDeletedAt(Instant.now());
        resourceRepository.save(resource);
    }

    @Transactional
    public Resource update(UUID id, UpdateResourceRequest request, User user) {
        Resource resource = resourceRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new RuntimeException("Resource not found"));

        resource.setUrl(request.getUrl());
        resource.setTitle(request.getTitle());
        resource.setNote(request.getNote());
        resource.setCategory(request.getCategory());

        Resource saved = resourceRepository.save(resource);

        // Handle tags
        if (request.getTags() != null) {
            // Remove existing tags
            resourceTagRepository.deleteByResource(resource);

            // Add new tags
            for (String tagName : request.getTags()) {
                Tag tag = tagRepository.findByNameAndUser(tagName, user)
                        .orElseGet(() -> {
                            Tag newTag = new Tag();
                            newTag.setName(tagName);
                            newTag.setUser(user);
                            return tagRepository.save(newTag);
                        });

                ResourceTag resourceTag = new ResourceTag();
                resourceTag.setResource(saved);
                resourceTag.setTag(tag);
                resourceTagRepository.save(resourceTag);
            }
        }

        return saved;
    }

    @Transactional
    public Resource toggleArchive(UUID id, User user) {
        Resource resource = resourceRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new RuntimeException("Resource not found"));

        resource.setIsArchived(!Boolean.TRUE.equals(resource.getIsArchived()));
        return resourceRepository.save(resource);
    }

    @Transactional
    public Resource togglePin(UUID id, User user) {
        Resource resource = resourceRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new RuntimeException("Resource not found"));

        resource.setIsPinned(!Boolean.TRUE.equals(resource.getIsPinned()));
        return resourceRepository.save(resource);
    }

    @Transactional
    public void bulkDelete(List<UUID> ids, User user) {
        for (UUID id : ids) {
            resourceRepository.findByIdAndUser(id, user).ifPresent(resource -> {
                resource.setIsDeleted(true);
                resource.setDeletedAt(Instant.now());
                resourceRepository.save(resource);
            });
        }
    }

    @Transactional
    public void bulkToggleArchive(List<UUID> ids, boolean archive, User user) {
        for (UUID id : ids) {
            Resource resource = resourceRepository.findByIdAndUser(id, user)
                    .orElseThrow(() -> new RuntimeException("Resource not found: " + id));
            resource.setIsArchived(archive);
            resourceRepository.save(resource);
        }
    }

    // --- Trash ---

    public List<Resource> getTrashResources(User user) {
        return resourceRepository.findByUserAndIsDeletedTrueOrderByDeletedAtDesc(user);
    }

    @Transactional
    public Resource restoreFromTrash(UUID id, User user) {
        Resource resource = resourceRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new RuntimeException("Resource not found"));
        resource.setIsDeleted(false);
        resource.setDeletedAt(null);
        return resourceRepository.save(resource);
    }

    @Transactional
    public void permanentDelete(UUID id, User user) {
        Resource resource = resourceRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new RuntimeException("Resource not found"));
        resourceRepository.delete(resource);
    }

    @Transactional
    public void emptyTrash(User user) {
        List<Resource> trashed = resourceRepository.findByUserAndIsDeletedTrueOrderByDeletedAtDesc(user);
        resourceRepository.deleteAll(trashed);
    }

    // --- Stats ---
    @Transactional
    public Map<String, Long> getStats(User user) {
        // Force refresh user from repository to get current persistent state
        User currentUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Migration: If persistent counter is 0 but user has resources, initialize it
        if (currentUser.getLifetimeResourcesCount() == 0) {
            long currentTotal = resourceRepository.countByUser(currentUser);
            if (currentTotal > 0) {
                currentUser.setLifetimeResourcesCount(currentTotal);
                userRepository.save(currentUser);
            }
        }

        Map<String, Long> stats = new HashMap<>();
        stats.put("lifetime", currentUser.getLifetimeResourcesCount());
        stats.put("active", resourceRepository.countByUserAndIsDeletedFalse(currentUser));
        stats.put("archived", resourceRepository.countByUserAndIsDeletedFalseAndIsArchivedTrue(currentUser));
        stats.put("deleted", resourceRepository.countByUserAndIsDeletedTrue(currentUser));
        return stats;
    }
}
