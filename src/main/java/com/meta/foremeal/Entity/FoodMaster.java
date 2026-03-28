package com.meta.foremeal.Entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name="FoodMaster")
@Getter
@NoArgsConstructor

public class FoodMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long foodId;


    @Column(nullable = false)
    private String foodName;

    private Double calories;
    private Double carbs;
    private Double protein;
    private Double fat;
    private Double sugar;
    private Double sodium;

    @Column(name="gi_index")
    private Double giIndex;

    public FoodMaster(String foodName, Double calories, Double sugar){
        this.foodName=foodName;
        this.calories=calories;
        this.sugar=sugar;
    }
}
