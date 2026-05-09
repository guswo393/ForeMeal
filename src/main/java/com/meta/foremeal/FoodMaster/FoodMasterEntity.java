package com.meta.foremeal.FoodMaster;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="food_master")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor

public class FoodMasterEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="food_id")
    private Long foodId;


    @Column(nullable = false)
    private String foodName;
    private String category;

    private Double calories;
    private Double carbs;
    private Double protein;
    private Double fat;
    private Double sugar;
    private Double sodium;

    @Column(name="gi_index")
    private Double giIndex;

    public FoodMasterEntity(String foodName, Double calories, Double sugar){
        this.foodName=foodName;
        this.calories=calories;
        this.sugar=sugar;
    }
}
