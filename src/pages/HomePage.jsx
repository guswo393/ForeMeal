import { useEffect, useState } from "react";
import "../styles/HomePage.css";

function HomePage() {
  // =========================
  // 혈당 그래프 데이터
  // 나중에 백엔드에서 받아올 예정
  // =========================
  const [graphData, setGraphData] = useState({
    pastLine: "",
    predictionYellow: "",
    predictionGreen: "",
  });

  // =========================
  // 추천 레시피 데이터
  // 나중에 백엔드에서 받아올 예정
  // =========================
  const [recipes, setRecipes] = useState([]);

  useEffect(() => {
    // ====================================
    // 임시 데이터
    // 나중에 fetch/axios로 교체 예정
    // ====================================

    setGraphData({
      pastLine:
        "M20 95 C45 85, 45 70, 70 82 C95 95, 95 35, 125 45 C155 55, 135 110, 170 105 C190 102, 185 130, 210 130 C245 130, 240 45, 280 45 C310 45, 300 105, 340 95",

      predictionYellow:
        "M340 95 C360 90, 365 55, 390 65 C410 72, 405 70, 430 66",

      predictionGreen:
        "M340 95 C365 94, 380 85, 430 96",
    });

    setRecipes([
      {
        id: 1,
        category: "양식",
        name: "에그마요샌드위치",
        sugar: 0,
        sodium: 0,
        image:
          "https://images.unsplash.com/photo-1528735602780-2552fd46c7af?auto=format&fit=crop&w=500&q=80",
      },
      {
        id: 2,
        category: "한식",
        name: "된장찌개",
        sugar: 0,
        sodium: 0,
        image:
          "https://images.unsplash.com/photo-1582878826629-29b7ad1cdc43?auto=format&fit=crop&w=500&q=80",
      },
      {
        id: 3,
        category: "한식",
        name: "김치찌개",
        sugar: 0,
        sodium: 0,
        image:
          "https://images.unsplash.com/photo-1604908176997-125f25cc6f3d?auto=format&fit=crop&w=500&q=80",
      },
    ]);

    // ====================================
    // 나중에 실제 백엔드 연결 시 예시
    // ====================================

    /*
    const fetchHomeData = async () => {
      try {
        const response = await axios.get(
          "http://localhost:8080/home"
        );

        setGraphData(response.data.graph);
        setRecipes(response.data.recipes);

      } catch (error) {
        console.error(error);
      }
    };

    fetchHomeData();
    */
  }, []);

  return (
    <div className="home-page">
      {/* 검색창 */}
      <div className="search-box">
        <span className="search-icon">⌕</span>

        <input type="text" placeholder="검색" />
      </div>


      {/* 혈당 그래프 */}
      <section className="section">
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

          <button className="more-btn">›</button>
        </div>

        <div className="recipe-list">
          {recipes.map((recipe) => (
            <div className="recipe-card" key={recipe.id}>
              <img
                src={recipe.image}
                alt={recipe.name}
              />

              <p className="recipe-category">
                {recipe.category}
              </p>

              <h3>{recipe.name}</h3>

              <strong>
                당류 {recipe.sugar}g, 나트륨 {recipe.sodium}g
              </strong>
            </div>
          ))}
        </div>
      </section>
    </div>
  );
}

export default HomePage;