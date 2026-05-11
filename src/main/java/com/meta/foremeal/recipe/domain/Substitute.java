package com.meta.foremeal.recipe.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "substitute")
public class Substitute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sub_id")
    private Long subId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private RecipeIngredient ingredient;

    @Column(name = "conversion_ratio", precision = 10, scale = 4)
    private BigDecimal conversionRatio;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    protected Substitute() {
    }

    public Substitute(BigDecimal conversionRatio, String description) {
        this.conversionRatio = conversionRatio;
        this.description = description;
    }

    void attach(RecipeIngredient ingredient) {
        this.ingredient = ingredient;
    }

    public Long getSubId() { return subId; }
    public BigDecimal getConversionRatio() { return conversionRatio; }
    public String getDescription() { return description; }
}
