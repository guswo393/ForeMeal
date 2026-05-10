package com.meta.foremeal.recipe.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "recipe_steps")
public class RecipeStep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "step_id")
    private Long stepId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id", nullable = false)
    private Recipe recipe;

    @Column(name = "step_order", nullable = false)
    private Integer stepOrder;

    @Column(name = "instruction", nullable = false, columnDefinition = "text")
    private String instruction;

    @Column(name = "image_uri", length = 500)
    private String imageUri;

    protected RecipeStep() {
    }

    public RecipeStep(Integer stepOrder, String instruction, String imageUri) {
        this.stepOrder = stepOrder;
        this.instruction = instruction;
        this.imageUri = imageUri;
    }

    void attach(Recipe recipe) {
        this.recipe = recipe;
    }

    public Long getStepId() { return stepId; }
    public Integer getStepOrder() { return stepOrder; }
    public String getInstruction() { return instruction; }
    public String getImageUri() { return imageUri; }
}
