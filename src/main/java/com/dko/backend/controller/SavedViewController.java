package com.dko.backend.controller;

import com.dko.backend.dto.FilterCriteria;
import com.dko.backend.dto.SavedViewRequest;
import com.dko.backend.dto.SavedViewResponse;
import com.dko.backend.model.SavedView;
import com.dko.backend.model.User;
import com.dko.backend.repository.UserRepository;
import com.dko.backend.security.SecurityUtils;
import com.dko.backend.service.SavedViewService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/saved-views")
@RequiredArgsConstructor
public class SavedViewController {

    private final SavedViewService savedViewService;
    private final UserRepository userRepository;

    @PostMapping
    public SavedViewResponse create(@RequestBody SavedViewRequest request) {
        try {
            User user = SecurityUtils.getCurrentUser(userRepository);
            SavedView view = savedViewService.create(request, user);
            return mapToResponse(view);
        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid filter criteria", e);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage(), e);
        }
    }

    @GetMapping
    public List<SavedViewResponse> myViews() {
        User user = SecurityUtils.getCurrentUser(userRepository);
        return savedViewService.getUserViews(user)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        User user = SecurityUtils.getCurrentUser(userRepository);
        savedViewService.delete(id, user);
    }

    private SavedViewResponse mapToResponse(SavedView view) {
        try {
            FilterCriteria filters = savedViewService.getFilters(view);
            return new SavedViewResponse(
                    view.getId(),
                    view.getName(),
                    filters,
                    view.getCreatedAt());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse filters", e);
        }
    }
}
