INSERT INTO food_master
    (food_name, category, calories, carbs, protein, fat, sugar, sodium, source)
SELECT '요거트', '유제품', 61, 4.7, 3.5, 3.3, 4.7, 46, 'LOCAL_SEED'
WHERE NOT EXISTS (
    SELECT 1 FROM food_master WHERE food_name = '요거트'
);

INSERT INTO food_master
    (food_name, category, calories, carbs, protein, fat, sugar, sodium, source)
SELECT '딸기', '과일', 32, 7.7, 0.7, 0.3, 4.9, 1, 'LOCAL_SEED'
WHERE NOT EXISTS (
    SELECT 1 FROM food_master WHERE food_name = '딸기'
);

WITH inserted_recipe AS (
    INSERT INTO recipe
        (title, external_id, source, description, category, dish_type, difficulty,
         cooking_time, servings, total_calories, total_nutrients, gi_level, image_uri)
    SELECT
        '바나나 요거트 볼',
        'local-banana-yogurt-bowl',
        'LOCAL_SEED',
        '냉장고 속 바나나와 요거트로 빠르게 만드는 아침 식사',
        '아침',
        '간편식',
        'EASY',
        5,
        1,
        210,
        '{"carbs":32,"protein":7,"fat":5}',
        'MEDIUM',
        NULL
    WHERE NOT EXISTS (
        SELECT 1 FROM recipe
        WHERE source = 'LOCAL_SEED'
          AND external_id = 'local-banana-yogurt-bowl'
    )
    RETURNING recipe_id
),
target_recipe AS (
    SELECT recipe_id FROM inserted_recipe
    UNION ALL
    SELECT recipe_id
    FROM recipe
    WHERE source = 'LOCAL_SEED'
      AND external_id = 'local-banana-yogurt-bowl'
    LIMIT 1
)
INSERT INTO recipe_ingredients
    (recipe_id, food_id, ingredient_name, quantity, unit)
SELECT target_recipe.recipe_id, food_master.food_id, food_master.food_name, ingredient.quantity, ingredient.unit
FROM target_recipe
JOIN (
    VALUES
        ('바나나', 1.00, '개'),
        ('요거트', 100.00, 'g')
) AS ingredient(food_name, quantity, unit) ON true
JOIN food_master ON food_master.food_name = ingredient.food_name
WHERE NOT EXISTS (
    SELECT 1
    FROM recipe_ingredients
    WHERE recipe_id = target_recipe.recipe_id
      AND ingredient_name = ingredient.food_name
);
