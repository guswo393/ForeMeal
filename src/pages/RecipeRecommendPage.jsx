import { useEffect, useState } from "react";
import "../styles/RecipeRecommendPage.css";

function RecipeRecommendPage({ userProfile, recommendationType = "health" }) {
  const [recipes, setRecipes] = useState([]);
  const [selectedRecipe, setSelectedRecipe] = useState(null);
  const [detailLoading, setDetailLoading] = useState(false);
  const [loading, setLoading] = useState(true);
  const isPantryRecommendation = recommendationType === "pantry";

  const pageTitle = isPantryRecommendation ? "내 재료로 만드는 레시피" : "추천 레시피";
  const pageSubtitle = isPantryRecommendation
    ? "냉장고에 등록된 재료로 만들 수 있는 음식이에요"
    : "혈당 관리를 위한 추천 음식이에요";

  useEffect(() => {
    fetchRecommendedRecipes();
  }, [userProfile?.userId, userProfile?.token, recommendationType]);

  const normalizeImageUrl = (imageUri) => {
    if (!imageUri) return "/images/recipe.jpg";
    if (imageUri.startsWith("http") || imageUri.startsWith("/")) return imageUri;
    return `/images/${imageUri}`;
  };

  const readNutrient = (nutrients, key) => {
    if (!nutrients) return null;

    try {
      const parsed = typeof nutrients === "string" ? JSON.parse(nutrients) : nutrients;
      return parsed[key] ?? parsed[key.toUpperCase()] ?? null;
    } catch {
      return null;
    }
  };

  const fetchRecommendedRecipes = async () => {
    try {
      setLoading(true);

      const headers = userProfile?.token
        ? { Authorization: `Bearer ${userProfile.token}` }
        : {};

      const recommendationPath = isPantryRecommendation ? "pantry" : "health";
      const recommendUrl = userProfile?.userId
        ? `/api/recipes/recommendations/${recommendationPath}?userId=${userProfile.userId}&limit=10`
        : "/api/recipes";

      const response = await fetch(recommendUrl, { headers });

      if (!response.ok) {
        throw new Error(`HTTP ${response.status}`);
      }

      const data = await response.json();

      setRecipes(
        data.map((recipe) => ({
          id: recipe.recipeId,
          name: recipe.title,
          calories: recipe.totalCalories ?? null,
          carbs: readNutrient(recipe.totalNutrients, "carbs"),
          protein: readNutrient(recipe.totalNutrients, "protein"),
          sugar: readNutrient(recipe.totalNutrients, "sugar"),
          sodium: readNutrient(recipe.totalNutrients, "sodium"),
          imageUrl: normalizeImageUrl(recipe.imageUri),
          description: recipe.description,
          giLevel: recipe.giLevel,
          reasons: recipe.reasons ?? [],
          matchRate: recipe.matchRate ?? 0,
          matchedIngredients: recipe.matchedIngredients ?? [],
          missingIngredientCount: recipe.missingIngredientCount ?? 0,
        }))
      );
    } catch (error) {
      console.error("추천 레시피 불러오기 실패:", error);
      setRecipes([]);
    } finally {
      setLoading(false);
    }
  };

  const handleOpenDetail = async (recipeId) => {
    try {
      setDetailLoading(true);

      const headers = userProfile?.token
        ? { Authorization: `Bearer ${userProfile.token}` }
        : {};

      const response = await fetch(`/api/recipes/${recipeId}`, { headers });

      if (!response.ok) {
        throw new Error(`HTTP ${response.status}`);
      }

      const data = await response.json();
      setSelectedRecipe(data);
    } catch (error) {
      console.error("레시피 상세 불러오기 실패:", error);
      alert("레시피 상세 정보를 불러오지 못했습니다.");
    } finally {
      setDetailLoading(false);
    }
  };

  return (
    <div className="recipe-page">
      <h1>{pageTitle}</h1>
      <p className="recipe-subtitle">{pageSubtitle}</p>

      {loading ? (
        <p>추천 음식을 불러오는 중...</p>
      ) : (
        <div className="recipe-list">
          {recipes.map((recipe) => (
            <div className="recipe-card" key={recipe.id}>
              <img
                src={recipe.imageUrl}
                alt={recipe.name}
                className="recipe-image"
              />

              <div className="recipe-content">
                <h2>{recipe.name}</h2>

                <div className="recipe-nutrition">
                  {recipe.calories != null && <span>{recipe.calories} kcal</span>}
                  {recipe.carbs != null && <span>탄수화물 {recipe.carbs}g</span>}
                  {recipe.protein != null && <span>단백질 {recipe.protein}g</span>}
                  {recipe.sugar != null && <span>당류 {recipe.sugar}g</span>}
                  {recipe.sodium != null && <span>나트륨 {recipe.sodium}mg</span>}
                  {recipe.giLevel && <span>GI {recipe.giLevel}</span>}
                </div>

                {isPantryRecommendation && (
                  <div className="recipe-match-info">
                    <strong>보유 재료 : {Math.round(recipe.matchRate * 100)}% 매칭</strong>
                    {recipe.matchedIngredients.length > 0 ? (
                      <span>{recipe.matchedIngredients.join(", ")}</span>
                    ) : (
                      <span>냉장고 재료와 직접 매칭된 항목이 없어요</span>
                    )}
                  </div>
                )}

                <button
                  className="recipe-detail-btn"
                  type="button"
                  onClick={() => handleOpenDetail(recipe.id)}
                  disabled={detailLoading}
                >
                  자세히 보기
                </button>
              </div>
            </div>
          ))}
        </div>
      )}

      {selectedRecipe && (
        <div className="recipe-detail-backdrop" onClick={() => setSelectedRecipe(null)}>
          <div className="recipe-detail-panel" onClick={(event) => event.stopPropagation()}>
            <button
              className="recipe-detail-close"
              type="button"
              onClick={() => setSelectedRecipe(null)}
            >
              ×
            </button>

            <img
              src={normalizeImageUrl(selectedRecipe.imageUri)}
              alt={selectedRecipe.title}
              className="recipe-detail-image"
            />

            <h2>{selectedRecipe.title}</h2>

            {selectedRecipe.description && (
              <p className="recipe-detail-description">{selectedRecipe.description}</p>
            )}

            <div className="recipe-detail-meta">
              {selectedRecipe.category && <span>{selectedRecipe.category}</span>}
              {selectedRecipe.cookingTime && <span>{selectedRecipe.cookingTime}분</span>}
              {selectedRecipe.servings && <span>{selectedRecipe.servings}인분</span>}
              {selectedRecipe.giLevel && <span>GI {selectedRecipe.giLevel}</span>}
            </div>

            <section className="recipe-detail-section">
              <h3>재료</h3>
              {selectedRecipe.ingredients?.length ? (
                <ul>
                  {selectedRecipe.ingredients.map((ingredient) => (
                    <li key={ingredient.itemId}>
                      {ingredient.ingredientName}
                      {ingredient.quantity && ` ${ingredient.quantity}`}
                      {ingredient.unit && ingredient.unit}
                    </li>
                  ))}
                </ul>
              ) : (
                <p>등록된 재료 정보가 없습니다.</p>
              )}
            </section>

            <section className="recipe-detail-section">
              <h3>조리방법</h3>
              {selectedRecipe.steps?.length ? (
                <ol>
                  {selectedRecipe.steps
                    .slice()
                    .sort((a, b) => a.stepOrder - b.stepOrder)
                    .map((step) => (
                      <li key={step.stepId}>{step.instruction}</li>
                    ))}
                </ol>
              ) : (
                <p>등록된 조리방법이 없습니다.</p>
              )}
            </section>
          </div>
        </div>
      )}
    </div>
  );
}

export default RecipeRecommendPage;
