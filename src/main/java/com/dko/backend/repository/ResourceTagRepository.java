package com.dko.backend.repository;

import com.dko.backend.model.ResourceTag;
import com.dko.backend.model.ResourceTagId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResourceTagRepository extends JpaRepository<ResourceTag, ResourceTagId> {
}
