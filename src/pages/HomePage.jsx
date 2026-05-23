import { useEffect, useState } from "react";
import "../styles/HomePage.css";

function HomePage({ userProfile, setCurrentPage, setRecipeRecommendType }) {
  const [graphData, setGraphData] = useState({
    pastLine: "",
    predictionYellow: "",
    predictionGreen: "",
  });

  const [recipes, setRecipes] = useState([]);
  const [pantryRecipes, setPantryRecipes] = useState([]);

  useEffect(() => {
    setGraphData({
      pastLine:
        "M20 95 C45 85, 45 70, 70 82 C95 95, 95 35, 125 45 C155 55, 135 110, 170 105 C190 102, 185 130, 210 130 C245 130, 240 45, 280 45 C310 45, 300 105, 340 95",

      predictionYellow:
        "M340 95 C360 90, 365 55, 390 65 C410 72, 405 70, 430 66",

      predictionGreen:
        "M340 95 C365 94, 380 85, 430 96",
    });

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

    const fetchRecipeDetails = async (recipes) => {
      return Promise.all(
        recipes.map(async (recipe) => {
          try {
            const detailResponse = await fetch(`/api/recipes/${recipe.recipeId}`, {
              headers: {
                Authorization: `Bearer ${userProfile.token}`,
              },
            });

            if (!detailResponse.ok) {
              throw new Error(`HTTP ${detailResponse.status}`);
            }

            const detail = await detailResponse.json();
            return {
              ...detail,
              matchedIngredientCount: recipe.matchedIngredientCount,
              missingIngredientCount: recipe.missingIngredientCount,
              matchRate: recipe.matchRate,
              matchedIngredients: recipe.matchedIngredients ?? [],
            };
          } catch (error) {
            console.error("홈 추천 레시피 상세 불러오기 실패:", error);
            return recipe;
          }
        })
      );
    };

    const mapRecipeCards = (recipes) =>
      recipes.map((recipe) => ({
        id: recipe.recipeId,
        category: recipe.category || recipe.giLevel || "추천",
        name: recipe.title,
        sugar: readNutrient(recipe.totalNutrients, "sugar"),
        sodium: readNutrient(recipe.totalNutrients, "sodium"),
        image: normalizeImageUrl(recipe.imageUri),
        nutritionConfidence: recipe.nutritionConfidence,
        matchRate: recipe.matchRate,
        matchedIngredients: recipe.matchedIngredients ?? [],
      }));

    const fetchRecipes = async () => {
      if (!userProfile?.token || !userProfile?.userId) {
        setRecipes([]);
        setPantryRecipes([]);
        return;
      }

      try {
        const headers = {
          Authorization: `Bearer ${userProfile.token}`,
        };

        const [healthResponse, pantryResponse] = await Promise.all([
          fetch(`/api/recipes/recommendations/health?userId=${userProfile.userId}&limit=5`, { headers }),
          fetch(`/api/recipes/recommendations/pantry?userId=${userProfile.userId}&limit=5`, { headers }),
        ]);

        if (!healthResponse.ok) {
          throw new Error(`health HTTP ${healthResponse.status}`);
        }

        if (!pantryResponse.ok) {
          throw new Error(`pantry HTTP ${pantryResponse.status}`);
        }

        const [healthData, pantryData] = await Promise.all([
          healthResponse.json(),
          pantryResponse.json(),
        ]);
        const [detailedHealthRecipes, detailedPantryRecipes] = await Promise.all([
          fetchRecipeDetails(healthData),
          fetchRecipeDetails(pantryData),
        ]);

        setRecipes(mapRecipeCards(detailedHealthRecipes));
        setPantryRecipes(mapRecipeCards(detailedPantryRecipes));
      } catch (error) {
        console.error("홈 추천 레시피 불러오기 실패:", error);
        setRecipes([]);
        setPantryRecipes([]);
      }
    };

    fetchRecipes();
  }, [userProfile?.userId, userProfile?.token]);

  return (
    <div className="home-page">
      {/* 검색창 */}
      <div className="search-box">
        <span className="search-icon">⌕</span>

        <input type="text" placeholder="검색" />
      </div>


      {/* 혈당 그래프 */}
      <section
        className="section glucose-section clickable-section"
        onClick={() => setCurrentPage("glucose")}
      >
        <h2>혈당 예측</h2>

        <div className="graph-box">
          <svg viewBox="0 0 460 170" className="glucose-graph">
            <line x1="20" y1="20" x2="20" y2="150" className="axis" />

            <line x1="20" y1="150" x2="445" y2="150" className="axis" />

            <path d={graphData.pastLine} className="line blue" />

            <path
              d={graphData.predictionYellow}
              className="line yellow"
            />

            <path
              d={graphData.predictionGreen}
              className="line green"
            />
          </svg>
        </div>
      </section>

      {/* 추천 레시피 */}
      <RecipeSection
        title="오늘의 추천 레시피"
        recipes={recipes}
        onOpen={() => {
          setRecipeRecommendType("health");
          setCurrentPage("recipeRecommend");
        }}
      />

      <RecipeSection
        title="내 재료로 만드는 레시피"
        recipes={pantryRecipes}
        showPantryMatch
        onOpen={() => {
          setRecipeRecommendType("pantry");
          setCurrentPage("recipeRecommend");
        }}
      />
    </div>
  );
}

function RecipeSection({ title, recipes, showPantryMatch = false, onOpen }) {
  const hasLowNutritionConfidence = (recipe) =>
    recipe?.nutritionConfidence != null && Number(recipe.nutritionConfidence) < 0.7;

  return (
    <section className="section recipe-section">
      <div className="section-title-row">
        <h2>{title}</h2>

        <button
          className="more-btn"
          type="button"
          onClick={onOpen}
        >
          ›
        </button>
      </div>

      <div className="home-recipe-list">
        {recipes.map((recipe) => (
          <button
            className="home-recipe-card"
            key={recipe.id}
            type="button"
            onClick={onOpen}
          >
            <img
              src={recipe.image}
              alt={recipe.name}
            />

            <p className="recipe-category">
              {recipe.category}
            </p>

            <h3>{recipe.name}</h3>

            {showPantryMatch && recipe.matchedIngredients.length > 0 && (
              <p className="pantry-match-text">
                보유 재료 : {recipe.matchedIngredients.slice(0, 2).join(", ")}
              </p>
            )}

            <strong>
              당류 {recipe.sugar ?? "-"}g, 나트륨 {recipe.sodium ?? "-"}mg
            </strong>

            {hasLowNutritionConfidence(recipe) && (
              <span className="home-nutrition-warning">
                섭취량 또는 영양값을 확인해주세요
              </span>
            )}
          </button>
        ))}

        {!recipes.length && (
          <button
            className="empty-recipe-card"
            type="button"
            onClick={onOpen}
          >
            추천 레시피를 확인해보세요
          </button>
        )}
      </div>
    </section>
  );
}

export default HomePage;
