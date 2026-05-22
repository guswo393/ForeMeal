import { useEffect, useState } from "react";
import "../styles/HomePage.css";

function HomePage({ userProfile, setCurrentPage }) {
  const [graphData, setGraphData] = useState({
    pastLine: "",
    predictionYellow: "",
    predictionGreen: "",
  });

  const [recipes, setRecipes] = useState([]);

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

    const fetchRecipes = async () => {
      if (!userProfile?.token || !userProfile?.userId) {
        setRecipes([]);
        return;
      }

      try {
        const response = await fetch(
          `/api/recipes/recommendations/health?userId=${userProfile.userId}&limit=5`,
          {
            headers: {
              Authorization: `Bearer ${userProfile.token}`,
            },
          }
        );

        if (!response.ok) {
          throw new Error(`HTTP ${response.status}`);
        }

        const data = await response.json();

        const detailedRecipes = await Promise.all(
          data.map(async (recipe) => {
            try {
              const detailResponse = await fetch(`/api/recipes/${recipe.recipeId}`, {
                headers: {
                  Authorization: `Bearer ${userProfile.token}`,
                },
              });

              if (!detailResponse.ok) {
                throw new Error(`HTTP ${detailResponse.status}`);
              }

              return detailResponse.json();
            } catch (error) {
              console.error("홈 추천 레시피 상세 불러오기 실패:", error);
              return recipe;
            }
          })
        );

        setRecipes(
          detailedRecipes.map((recipe) => ({
            id: recipe.recipeId,
            category: recipe.category || recipe.giLevel || "추천",
            name: recipe.title,
            sugar: readNutrient(recipe.totalNutrients, "sugar"),
            sodium: readNutrient(recipe.totalNutrients, "sodium"),
            image: normalizeImageUrl(recipe.imageUri),
          }))
        );
      } catch (error) {
        console.error("홈 추천 레시피 불러오기 실패:", error);
        setRecipes([]);
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
        className="section clickable-section"
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
      <section className="section recipe-section">
        <div className="section-title-row">
          <h2>오늘의 추천 레시피</h2>

          <button
            className="more-btn"
            type="button"
            onClick={() => setCurrentPage("recipeRecommend")}
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
              onClick={() => setCurrentPage("recipeRecommend")}
            >
              <img
                src={recipe.image}
                alt={recipe.name}
              />

              <p className="recipe-category">
                {recipe.category}
              </p>

              <h3>{recipe.name}</h3>

              <strong>
                당류 {recipe.sugar ?? "-"}g, 나트륨 {recipe.sodium ?? "-"}g
              </strong>
            </button>
          ))}

          {!recipes.length && (
            <button
              className="empty-recipe-card"
              type="button"
              onClick={() => setCurrentPage("recipeRecommend")}
            >
              추천 레시피를 확인해보세요
            </button>
          )}
        </div>
      </section>
    </div>
  );
}

export default HomePage;
