INSERT INTO food_master
    (food_name, category, calories, carbs, protein, fat, sugar, sodium, source)
SELECT '바나나', '과일', 89, 22.8, 1.1, 0.3, 12.2, 1, 'LOCAL_SEED'
WHERE NOT EXISTS (
    SELECT 1 FROM food_master WHERE food_name = '바나나'
);

UPDATE ingredient_alias
SET food_id = (
        SELECT food_id
        FROM food_master
        WHERE food_name = '바나나'
        ORDER BY food_id
        LIMIT 1
    ),
    updated_at = CURRENT_TIMESTAMP
WHERE lower(detected_name) IN ('banana', 'bananas');
