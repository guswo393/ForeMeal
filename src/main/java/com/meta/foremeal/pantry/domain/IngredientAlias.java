package com.meta.foremeal.pantry.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.meta.foremeal.foodmaster.domain.FoodMasterEntity;

import java.time.LocalDateTime;

@Entity
@Table(name = "ingredient_alias")
@Getter
@NoArgsConstructor
public class IngredientAlias {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "alias_id")
    private Long aliasId;

    @Column(name = "detected_name", nullable = false, unique = true, length = 100)
    private String detectedName;

    @Column(name = "search_keyword", nullable = false, length = 100)
    private String searchKeyword;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_id")
    private FoodMasterEntity foodMaster;

    @Column(name = "source", length = 30)
    private String source;

    @Column(name = "is_verified", nullable = false)
    private boolean verified = false;

    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
        if (source == null) {
            source = "SYSTEM";
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
