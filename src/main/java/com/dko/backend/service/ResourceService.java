package com.dko.backend.service;

import com.dko.backend.model.Resource;
import com.dko.backend.model.ResourceTag;
import com.dko.backend.model.Tag;
import com.dko.backend.model.User;
import com.dko.backend.repository.ResourceRepository;
import com.dko.backend.repository.ResourceTagRepository;
import com.dko.backend.repository.TagRepository;
import com.dko.backend.dto.CreateResourceRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

    public List<Resource> getUserResources(User user) {
        return resourceRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public void delete(UUID id, User user) {
        Resource resource = resourceRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new RuntimeException("Resource not found"));

        resourceRepository.delete(resource);
    }
}
