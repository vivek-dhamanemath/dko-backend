package com.dko.backend.controller;

import com.dko.backend.model.CategoryColor;
import com.dko.backend.model.User;
import com.dko.backend.repository.CategoryColorRepository;
import com.dko.backend.repository.UserRepository;
import com.dko.backend.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/category-colors")
@RequiredArgsConstructor
public class CategoryColorController {

    private final CategoryColorRepository categoryColorRepository;
    private final UserRepository userRepository;

    @GetMapping
    public Map<String, String> getAll() {
        User user = SecurityUtils.getCurrentUser(userRepository);
        return categoryColorRepository.findByUser(user).stream()
                .collect(Collectors.toMap(CategoryColor::getCategoryName, CategoryColor::getColor));
    }

    @PutMapping
    public Map<String, String> setColor(@RequestBody Map<String, String> body) {
        User user = SecurityUtils.getCurrentUser(userRepository);
        String categoryName = body.get("category");
        String color = body.get("color");

        CategoryColor cc = categoryColorRepository.findByUserAndCategoryName(user, categoryName)
                .orElseGet(() -> {
                    CategoryColor newCc = new CategoryColor();
                    newCc.setUser(user);
                    newCc.setCategoryName(categoryName);
                    return newCc;
                });
        cc.setColor(color);
        categoryColorRepository.save(cc);

        // Return full map
        return categoryColorRepository.findByUser(user).stream()
                .collect(Collectors.toMap(CategoryColor::getCategoryName, CategoryColor::getColor));
    }
}
