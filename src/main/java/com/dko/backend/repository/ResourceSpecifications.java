package com.dko.backend.repository;

import com.dko.backend.model.Resource;
import com.dko.backend.model.ResourceTag;
import com.dko.backend.model.Tag;
import com.dko.backend.model.User;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

public class ResourceSpecifications {

    public static Specification<Resource> belongsToUser(User user) {
        return (root, query, cb) -> cb.equal(root.get("user"), user);
    }

    public static Specification<Resource> hasCategories(List<String> categories) {
        return (root, query, cb) -> {
            if (categories == null || categories.isEmpty()) {
                return null;
            }
            return root.get("category").in(categories);
        };
    }

    public static Specification<Resource> hasTags(List<String> tagNames) {
        return (root, query, cb) -> {
            if (tagNames == null || tagNames.isEmpty()) {
                return null;
            }

            // Join Resource -> ResourceTag -> Tag
            Join<Resource, ResourceTag> resourceTagJoin = root.join("resourceTags");
            Join<ResourceTag, Tag> tagJoin = resourceTagJoin.join("tag");

            // Ensure distinct results
            query.distinct(true);

            return tagJoin.get("name").in(tagNames);
        };
    }

    public static Specification<Resource> createdBetween(LocalDate start, LocalDate end) {
        return (root, query, cb) -> {
            if (start == null || end == null) {
                return null;
            }

            Instant startInstant = start.atStartOfDay().toInstant(ZoneOffset.UTC);
            Instant endInstant = end.atTime(23, 59, 59).toInstant(ZoneOffset.UTC);

            return cb.between(root.get("createdAt"), startInstant, endInstant);
        };
    }
}
