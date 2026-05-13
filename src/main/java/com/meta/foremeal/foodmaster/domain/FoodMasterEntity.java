package com.meta.foremeal.foodmaster.domain;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="FoodMaster")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor

public class FoodMasterEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long foodId;

    @Column(length = 100)
    private String externalId;

    @Column(length = 50)
    private String source;

    @Column(nullable = false)
    private String foodName;
    private String category;
    private String makerName;
    private String researchYear;
    private String subRefName;
    private Double servingSize;
    private String servingUnit;

    private Double calories;
    private Double carbs;
    private Double protein;
    private Double fat;
    private Double sugar;
    private Double sodium;
    private Double cholesterol;
    private Double saturatedFat;
    private Double transFat;

    @Column(name="gi_index")
    private Double giIndex;

    public FoodMasterEntity(String foodName, Double calories, Double sugar){
        this.foodName=foodName;
        this.calories=calories;
        this.sugar=sugar;
    }
}
