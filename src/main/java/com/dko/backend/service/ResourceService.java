package com.dko.backend.service;

import com.dko.backend.model.Resource;
import com.dko.backend.model.User;
import com.dko.backend.repository.ResourceRepository;
import com.dko.backend.dto.CreateResourceRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ResourceService {

    private final ResourceRepository resourceRepository;

    public Resource create(CreateResourceRequest request, User user) {
        Resource resource = new Resource();
        resource.setTitle(request.getTitle());
        resource.setUrl(request.getUrl());
        resource.setDescription(request.getDescription());
        resource.setOwner(user);

        return resourceRepository.save(resource);
    }

    public List<Resource> getUserResources(User user) {
        return resourceRepository.findByOwner(user);
    }

    public void delete(UUID id, User user) {
        Resource resource = resourceRepository.findByIdAndOwner(id, user)
                .orElseThrow(() -> new RuntimeException("Resource not found"));

        resourceRepository.delete(resource);
    }
}
