package com.meta.foremeal.health.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "health_profile",
        uniqueConstraints = @UniqueConstraint(columnNames = "user_id")
)
public class HealthProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "health_profile_id")
    private Long healthProfileId;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "has_diabetes", nullable = false)
    private boolean hasDiabetes;

    @Column(name = "has_hypertension", nullable = false)
    private boolean hasHypertension;

    @Column(name = "height_cm", precision = 5, scale = 2)
    private BigDecimal heightCm;

    @Column(name = "weight_kg", precision = 5, scale = 2)
    private BigDecimal weightKg;

    @Enumerated(EnumType.STRING)
    @Column(name = "goal", length = 50)
    private HealthGoal goal;

    @Column(name = "allergy_notes", columnDefinition = "text")
    private String allergyNotes;

    @Column(name = "avoid_ingredients", columnDefinition = "text")
    private String avoidIngredients;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    protected HealthProfile() {
    }

    public HealthProfile(Long userId,
                         boolean hasDiabetes,
                         boolean hasHypertension,
                         BigDecimal heightCm,
                         BigDecimal weightKg,
                         HealthGoal goal,
                         String allergyNotes,
                         String avoidIngredients) {
        this.userId = userId;
        this.hasDiabetes = hasDiabetes;
        this.hasHypertension = hasHypertension;
        this.heightCm = heightCm;
        this.weightKg = weightKg;
        this.goal = goal;
        this.allergyNotes = allergyNotes;
        this.avoidIngredients = avoidIngredients;
    }

    public void update(boolean hasDiabetes,
                       boolean hasHypertension,
                       BigDecimal heightCm,
                       BigDecimal weightKg,
                       HealthGoal goal,
                       String allergyNotes,
                       String avoidIngredients) {
        this.hasDiabetes = hasDiabetes;
        this.hasHypertension = hasHypertension;
        this.heightCm = heightCm;
        this.weightKg = weightKg;
        this.goal = goal;
        this.allergyNotes = allergyNotes;
        this.avoidIngredients = avoidIngredients;
        this.updatedAt = LocalDateTime.now();
    }

    public Long getHealthProfileId() {
        return healthProfileId;
    }

    public Long getUserId() {
        return userId;
    }

    public boolean isHasDiabetes() {
        return hasDiabetes;
    }

    public boolean isHasHypertension() {
        return hasHypertension;
    }

    public BigDecimal getHeightCm() {
        return heightCm;
    }

    public BigDecimal getWeightKg() {
        return weightKg;
    }

    public HealthGoal getGoal() {
        return goal;
    }

    public String getAllergyNotes() {
        return allergyNotes;
    }

    public String getAvoidIngredients() {
        return avoidIngredients;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}