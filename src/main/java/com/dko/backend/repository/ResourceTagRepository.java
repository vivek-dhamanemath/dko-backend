package com.dko.backend.repository;

import com.dko.backend.model.Resource;
import com.dko.backend.model.ResourceTag;
import com.dko.backend.model.ResourceTagId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResourceTagRepository extends JpaRepository<ResourceTag, ResourceTagId> {
    List<ResourceTag> findByResource(Resource resource);
}
