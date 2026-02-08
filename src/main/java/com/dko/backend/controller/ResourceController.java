package com.dko.backend.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dko.backend.dto.CreateResourceRequest;
import com.dko.backend.dto.FilterCriteria;
import com.dko.backend.dto.ResourceResponse;
import com.dko.backend.model.Resource;
import com.dko.backend.model.User;
import com.dko.backend.repository.ResourceTagRepository;
import com.dko.backend.repository.UserRepository;
import com.dko.backend.security.SecurityUtils;
import com.dko.backend.service.ResourceService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/resources")
@RequiredArgsConstructor
public class ResourceController {

    private final ResourceService resourceService;
    private final UserRepository userRepository;
    private final ResourceTagRepository resourceTagRepository;

    @PostMapping
    public ResourceResponse create(
            @RequestBody CreateResourceRequest request) {
        User user = SecurityUtils.getCurrentUser(userRepository);
        Resource resource = resourceService.create(request, user);

        return map(resource);
    }

    @GetMapping
    public List<ResourceResponse> myResources() {
        User user = SecurityUtils.getCurrentUser(userRepository);

        return resourceService.getUserResources(user)
                .stream()
                .map(this::map)
                .toList();
    }

    @PostMapping("/filter")
    public List<ResourceResponse> filterResources(
            @RequestBody FilterCriteria criteria) {
        User user = SecurityUtils.getCurrentUser(userRepository);

        return resourceService.getFilteredResources(user, criteria)
                .stream()
                .map(this::map)
                .toList();
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        User user = SecurityUtils.getCurrentUser(userRepository);
        resourceService.delete(id, user);
    }

    private ResourceResponse map(Resource r) {
        List<String> tags = resourceTagRepository.findByResource(r)
                .stream()
                .map(rt -> rt.getTag().getName())
                .toList();

        return new ResourceResponse(
                r.getId(),
                r.getUrl(),
                r.getTitle(),
                r.getNote(),
                r.getCategory(),
                tags,
                r.getCreatedAt());
    }
}
