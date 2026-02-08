package com.dko.backend.service;

import com.dko.backend.dto.FilterCriteria;
import com.dko.backend.dto.SavedViewRequest;
import com.dko.backend.model.SavedView;
import com.dko.backend.model.User;
import com.dko.backend.repository.SavedViewRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SavedViewService {

    private final SavedViewRepository savedViewRepository;
    private final ObjectMapper objectMapper;

    public SavedView create(SavedViewRequest request, User user) throws JsonProcessingException {
        // Check if view with same name exists
        savedViewRepository.findByNameAndUser(request.getName(), user)
                .ifPresent(existing -> {
                    throw new RuntimeException("A view with name '" + request.getName() + "' already exists");
                });

        SavedView view = new SavedView();
        view.setUser(user);
        view.setName(request.getName());
        view.setFilters(objectMapper.writeValueAsString(request.getFilters()));

        return savedViewRepository.save(view);
    }

    public List<SavedView> getUserViews(User user) {
        return savedViewRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public FilterCriteria getFilters(SavedView view) throws JsonProcessingException {
        return objectMapper.readValue(view.getFilters(), FilterCriteria.class);
    }

    public void delete(UUID id, User user) {
        SavedView view = savedViewRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new RuntimeException("Saved view not found"));
        savedViewRepository.delete(view);
    }
}
