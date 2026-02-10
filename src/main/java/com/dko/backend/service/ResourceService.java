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
import com.dko.backend.dto.CreateResourceRequest;
import com.dko.backend.dto.UpdateResourceRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ResourceService {

    private final ResourceRepository resourceRepository;
    private final TagRepository tagRepository;
    private final ResourceTagRepository resourceTagRepository;

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

        // Handle archive status (default to false if null)
        boolean includeArchived = criteria.getIsArchived() != null ? criteria.getIsArchived() : false;
        spec = spec.and(ResourceSpecifications.isArchived(includeArchived));

        // Execute query with sorting
        return resourceRepository.findAll(spec,
                Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    public void delete(UUID id, User user) {
        Resource resource = resourceRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new RuntimeException("Resource not found"));

        resourceRepository.delete(resource);
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
}
