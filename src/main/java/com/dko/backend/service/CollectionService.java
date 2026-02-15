package com.dko.backend.service;

import com.dko.backend.model.Collection;
import com.dko.backend.model.Resource;
import com.dko.backend.model.User;
import com.dko.backend.repository.CollectionRepository;
import com.dko.backend.repository.ResourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CollectionService {

    private final CollectionRepository collectionRepository;
    private final ResourceRepository resourceRepository;

    public List<Collection> getCollections(User user) {
        return collectionRepository.findByUser(user);
    }

    @Transactional
    public Collection createCollection(String name, User user) {
        Collection collection = new Collection();
        collection.setName(name);
        collection.setUser(user);
        return collectionRepository.save(collection);
    }

    @Transactional
    public void addResourceToCollection(UUID collectionId, UUID resourceId, User user) {
        Collection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new RuntimeException("Collection not found"));

        if (!collection.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new RuntimeException("Resource not found"));

        if (!resource.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        collection.getResources().add(resource);
        collectionRepository.save(collection);
    }

    @Transactional
    public void removeResourceFromCollection(UUID collectionId, UUID resourceId, User user) {
        Collection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new RuntimeException("Collection not found"));

        if (!collection.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new RuntimeException("Resource not found"));

        collection.getResources().remove(resource);
        collectionRepository.save(collection);
    }

    @Transactional
    public void deleteCollection(UUID collectionId, User user) {
        Collection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new RuntimeException("Collection not found"));

        if (!collection.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        collectionRepository.delete(collection);
    }

    public Collection getCollection(UUID id, User user) {
        Collection collection = collectionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Collection not found"));

        if (!collection.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        return collection;
    }

    @Transactional
    public void addResourcesToCollection(UUID collectionId, List<UUID> resourceIds, User user) {
        Collection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new RuntimeException("Collection not found"));

        if (!collection.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        List<Resource> resources = resourceRepository.findAllById(resourceIds);

        // Verify ownership for all resources
        for (Resource resource : resources) {
            if (!resource.getUser().getId().equals(user.getId())) {
                throw new RuntimeException("Unauthorized resource addition");
            }
        }

        collection.getResources().addAll(resources);
        collectionRepository.save(collection);
    }
}
