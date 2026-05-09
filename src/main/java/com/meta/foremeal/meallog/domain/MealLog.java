package com.meta.foremeal.meallog.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "meal_log")
public class MealLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "meal_id")
    private Long mealId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "eaten_at", nullable = false)
    private LocalDateTime eatenAt;

    @Column(name = "notes", columnDefinition = "text")
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false, length = 30)
    private MealSource source;

    @Column(name = "recipe_id")
    private Long recipeId;

    @OneToMany(mappedBy = "mealLog", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MealLogItem> items = new ArrayList<>();

    protected MealLog() {}

    public MealLog(Long userId, LocalDateTime eatenAt, String notes, MealSource source, Long recipeId) {
        this.userId = userId;
        this.eatenAt = eatenAt;
        this.notes = notes;
        this.source = source;
        this.recipeId = recipeId;
    }

    public void addItem(MealLogItem item) {
        item.attach(this);
        this.items.add(item);
    }

    public Long getMealId() { return mealId; }
    public Long getUserId() { return userId; }
    public LocalDateTime getEatenAt() { return eatenAt; }
    public String getNotes() { return notes; }
    public MealSource getSource() { return source; }
    public Long getRecipeId() { return recipeId; }
    public List<MealLogItem> getItems() { return items; }

    public void update(LocalDateTime eatenAt, String notes, MealSource source, Long recipeId) {
        this.eatenAt = eatenAt;
        this.notes = notes;
        this.source = source;
        this.recipeId = recipeId;
    }
}