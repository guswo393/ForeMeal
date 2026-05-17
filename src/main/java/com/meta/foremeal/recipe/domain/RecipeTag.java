package com.meta.foremeal.recipe.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "RecipeTag")
@Getter @Setter
@NoArgsConstructor
public class RecipeTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tag_id")
    private Long tagId; // 태그고유 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id")
    private Recipe Recipe;

    @Column(name = "tag_name")
    private String tagName; // 태그네임 (#비건 등)
}