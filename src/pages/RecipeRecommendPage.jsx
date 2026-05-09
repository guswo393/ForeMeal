import { useEffect, useState } from "react";
import "../styles/RecipeRecommendPage.css";

function RecipeRecommendPage() {
  const [recipes, setRecipes] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchRecommendedRecipes();
  }, []);

  const fetchRecommendedRecipes = async () => {
    try {
      setLoading(true);

      // 나중에 백엔드 연결 시 이 주소만 실제 API 주소로 바꾸면 됨
      // const response = await fetch("http://localhost:8080/api/recipes/recommend");
      // const data = await response.json();
      // setRecipes(data);

      // 임시 데이터
      const data = [
        {
          id: 1,
          name: "닭가슴살 샐러드",
          calories: 320,
          carbs: 12,
          protein: 35,
          sugar: 4,
          imageUrl: "/images/chicken-salad.png",
        },
        {
          id: 2,
          name: "현미밥 연어구이",
          calories: 450,
          carbs: 42,
          protein: 30,
          sugar: 3,
          imageUrl: "/images/salmon-rice.png",
        },
        {
          id: 3,
          name: "두부 야채볶음",
          calories: 280,
          carbs: 18,
          protein: 22,
          sugar: 5,
          imageUrl: "/images/tofu.png",
        },
      ];

      setRecipes(data);
    } catch (error) {
      console.error("추천 레시피 불러오기 실패:", error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="recipe-page">
      <h1>추천 레시피</h1>
      <p className="recipe-subtitle">혈당 관리를 위한 추천 음식이에요</p>

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
                  <span>{recipe.calories} kcal</span>
                  <span>탄수화물 {recipe.carbs}g</span>
                  <span>단백질 {recipe.protein}g</span>
                  <span>당류 {recipe.sugar}g</span>
                </div>

                <button className="recipe-detail-btn">자세히 보기</button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

export default RecipeRecommendPage;