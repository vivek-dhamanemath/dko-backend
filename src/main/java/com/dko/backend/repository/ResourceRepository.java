package com.dko.backend.repository;

import com.dko.backend.model.Resource;
import com.dko.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ResourceRepository extends JpaRepository<Resource, UUID> {

    List<Resource> findByUser(User user);
}
