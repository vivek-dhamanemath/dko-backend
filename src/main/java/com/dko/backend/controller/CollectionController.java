package com.dko.backend.controller;

import com.dko.backend.dto.CollectionResponse;
import com.dko.backend.dto.MessageResponse;
import com.dko.backend.model.Collection;
import com.dko.backend.model.User;
import com.dko.backend.service.CollectionService;
import com.dko.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.dko.backend.security.SecurityUtils;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/collections")
@RequiredArgsConstructor
public class CollectionController {

        private final CollectionService collectionService;
        private final UserRepository userRepository;

        @GetMapping
        public ResponseEntity<List<CollectionResponse>> getCollections() {
                User user = SecurityUtils.getCurrentUser(userRepository);
                List<CollectionResponse> responses = collectionService.getCollections(user).stream()
                                .map(c -> new CollectionResponse(c.getId(), c.getName(), c.getCreatedAt()))
                                .collect(Collectors.toList());
                return ResponseEntity.ok(responses);
        }

        @PostMapping
        public ResponseEntity<CollectionResponse> createCollection(@RequestBody String name) {
                User user = SecurityUtils.getCurrentUser(userRepository);
                Collection collection = collectionService.createCollection(name, user);
                return ResponseEntity
                                .ok(new CollectionResponse(collection.getId(), collection.getName(),
                                                collection.getCreatedAt()));
        }

        @PostMapping("/{collectionId}/add/{resourceId}")
        public ResponseEntity<MessageResponse> addResource(@PathVariable UUID collectionId,
                        @PathVariable UUID resourceId) {
                User user = SecurityUtils.getCurrentUser(userRepository);
                collectionService.addResourceToCollection(collectionId, resourceId, user);
                return ResponseEntity.ok(new MessageResponse("Resource added to collection"));
        }

        @DeleteMapping("/{collectionId}/remove/{resourceId}")
        public ResponseEntity<MessageResponse> removeResource(@PathVariable UUID collectionId,
                        @PathVariable UUID resourceId) {
                User user = SecurityUtils.getCurrentUser(userRepository);
                collectionService.removeResourceFromCollection(collectionId, resourceId, user);
                return ResponseEntity.ok(new MessageResponse("Resource removed from collection"));
        }

        @PostMapping("/{collectionId}/add-multiple")
        public ResponseEntity<MessageResponse> addResources(@PathVariable UUID collectionId,
                        @RequestBody List<UUID> resourceIds) {
                User user = SecurityUtils.getCurrentUser(userRepository);
                collectionService.addResourcesToCollection(collectionId, resourceIds, user);
                return ResponseEntity.ok(new MessageResponse("Resources added to collection"));
        }

        @DeleteMapping("/{collectionId}")
        public ResponseEntity<MessageResponse> deleteCollection(@PathVariable UUID collectionId) {
                User user = SecurityUtils.getCurrentUser(userRepository);
                collectionService.deleteCollection(collectionId, user);
                return ResponseEntity.ok(new MessageResponse("Collection deleted"));
        }
}
