import "../styles/MealRecommendPage.css";

function MealRecommendPage({ setCurrentPage }) {
  const menuItems = [
    {
      id: 1,
      title: "추천 레시피",
      image: "/images/recipe.png",
      page: "recipeRecommend",
    },
    {
      id: 2,
      title: "외식, 배달",
      image: "/images/delivery.png",
      page: "delivery",
    },
    {
      id: 3,
      title: "수치환산",
      image: "/images/calculator.png",
      page: "calculator",
    },
  ];

  return (
    <div className="meal-page">
      {menuItems.map((item) => (
        <div className="meal-card" key={item.id}>
          <img src={item.image} alt={item.title} className="meal-image" />

          <div className="meal-info">
            <h2>{item.title}</h2>
            <button onClick={() => setCurrentPage(item.page)}>선택</button>
          </div>
        </div>
      ))}
    </div>
  );
}

export default MealRecommendPage;