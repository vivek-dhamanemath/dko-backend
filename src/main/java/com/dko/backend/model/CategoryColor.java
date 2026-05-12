package com.dko.backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "category_colors", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "category_name"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryColor {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "category_name", nullable = false, length = 100)
    private String categoryName;

    @Column(nullable = false, length = 20)
    private String color;
}
