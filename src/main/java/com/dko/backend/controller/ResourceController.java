package com.dko.backend.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.dko.backend.dto.CreateResourceRequest;
import com.dko.backend.dto.UpdateResourceRequest;
import com.dko.backend.dto.FilterCriteria;
import com.dko.backend.dto.ResourceResponse;
import com.dko.backend.model.Resource;
import com.dko.backend.model.User;
import com.dko.backend.repository.ResourceTagRepository;
import com.dko.backend.repository.UserRepository;
import com.dko.backend.security.SecurityUtils;
import com.dko.backend.service.ResourceService;
import com.dko.backend.service.MetadataService;
import com.dko.backend.dto.MetadataResponse;
import com.dko.backend.dto.CollectionResponse;
import com.dko.backend.model.Collection;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/resources")
@RequiredArgsConstructor
public class ResourceController {

    private final ResourceService resourceService;
    private final MetadataService metadataService;
    private final UserRepository userRepository;
    private final ResourceTagRepository resourceTagRepository;

    @PostMapping
    public ResourceResponse create(
            @RequestBody CreateResourceRequest request) {
        User user = SecurityUtils.getCurrentUser(userRepository);
        Resource resource = resourceService.create(request, user);

        return map(resource);
    }

    @GetMapping("/metadata")
    public MetadataResponse fetchMetadata(@RequestParam String url) {
        return metadataService.fetchMetadata(url);
    }

    @PutMapping("/{id}")
    public ResourceResponse update(
            @PathVariable UUID id,
            @RequestBody UpdateResourceRequest request) {
        User user = SecurityUtils.getCurrentUser(userRepository);
        Resource resource = resourceService.update(id, request, user);
        return map(resource);
    }

    @PutMapping("/{id}/archive")
    public ResourceResponse toggleArchive(@PathVariable UUID id) {
        User user = SecurityUtils.getCurrentUser(userRepository);
        Resource resource = resourceService.toggleArchive(id, user);
        return map(resource);
    }

    @PutMapping("/{id}/pin")
    public ResourceResponse togglePin(@PathVariable UUID id) {
        User user = SecurityUtils.getCurrentUser(userRepository);
        Resource resource = resourceService.togglePin(id, user);
        return map(resource);
    }

    @GetMapping
    public List<ResourceResponse> myResources(
            @RequestParam(required = false, defaultValue = "false") boolean archived) {
        User user = SecurityUtils.getCurrentUser(userRepository);

        return resourceService.getUserResources(user, archived)
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

    @PostMapping("/bulk-delete")
    public void bulkDelete(@RequestBody List<UUID> ids) {
        User user = SecurityUtils.getCurrentUser(userRepository);
        resourceService.bulkDelete(ids, user);
    }

    @PutMapping("/bulk/archive")
    public void bulkArchive(@RequestBody List<UUID> ids, @RequestParam boolean archive) {
        User user = SecurityUtils.getCurrentUser(userRepository);
        resourceService.bulkToggleArchive(ids, archive, user);
    }

    // --- Trash ---

    @GetMapping("/trash")
    public List<ResourceResponse> getTrash() {
        User user = SecurityUtils.getCurrentUser(userRepository);
        return resourceService.getTrashResources(user)
                .stream()
                .map(this::map)
                .toList();
    }

    @PutMapping("/{id}/restore")
    public ResourceResponse restore(@PathVariable UUID id) {
        User user = SecurityUtils.getCurrentUser(userRepository);
        Resource resource = resourceService.restoreFromTrash(id, user);
        return map(resource);
    }

    @DeleteMapping("/{id}/permanent")
    public void permanentDelete(@PathVariable UUID id) {
        User user = SecurityUtils.getCurrentUser(userRepository);
        resourceService.permanentDelete(id, user);
    }

    @DeleteMapping("/trash/empty")
    public void emptyTrash() {
        User user = SecurityUtils.getCurrentUser(userRepository);
        resourceService.emptyTrash(user);
    }

    // --- Stats ---

    @GetMapping("/stats")
    public Map<String, Long> getStats() {
        User user = SecurityUtils.getCurrentUser(userRepository);
        return resourceService.getStats(user);
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
                r.getCreatedAt(),
                r.getIsArchived() != null ? r.getIsArchived() : false,
                r.getIsDeleted() != null ? r.getIsDeleted() : false,
                r.getIsPinned() != null ? r.getIsPinned() : false,
                r.getDeletedAt(),
                r.getCollections().stream()
                        .map(c -> new CollectionResponse(c.getId(), c.getName(), c.getCreatedAt()))
                        .toList());
    }
}
