package com.dko.backend.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "resource_tags")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@IdClass(ResourceTagId.class)
public class ResourceTag {

    @Id
    @ManyToOne
    @JoinColumn(name = "resource_id")
    private Resource resource;

    @Id
    @ManyToOne
    @JoinColumn(name = "tag_id")
    private Tag tag;
}
