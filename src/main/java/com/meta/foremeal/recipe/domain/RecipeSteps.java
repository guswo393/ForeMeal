package com.meta.foremeal.recipe.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "RecipeSteps")
@Getter @Setter
@NoArgsConstructor
public class RecipeSteps {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "step_id")
    private Long stepId; // 단계고유 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id")
    private Recipe Recipe;

    @Column(name = "step_order")
    private Integer stepOrder; // 조리순서

    @Column(name = "instruction", columnDefinition = "TEXT")
    private String instruction; // 조리설명

    @Column(name = "image_url")
    private String imageUrl; // 이미지경로
}