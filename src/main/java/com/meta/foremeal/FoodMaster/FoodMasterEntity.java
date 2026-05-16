package com.meta.foremeal.FoodMaster;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name="food_master")
@Getter
@NoArgsConstructor

public class FoodMasterEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="food_id")
    private Long foodId;


    @Column(name="food_name", nullable = false)
    private String foodName;

    private String category;
    private Double calories;
    private Double carbs;
    private Double protein;
    private Double fat;
    private Double sodium;
    private Double sugar;

    @Column(name="gi_index")
    private Double giIndex;

    public FoodMasterEntity(String foodName, Double calories, Double sugar){
        this.foodName=foodName;
        this.calories=calories;
        this.sugar=sugar;
    }
}
