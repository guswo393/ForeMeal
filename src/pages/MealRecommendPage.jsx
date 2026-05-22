import "../styles/MealRecommendPage.css";

function MealRecommendPage({ setCurrentPage }) {
  const menuItems = [
    {
      id: 1,
      title: "추천 레시피",
      image: "/images/recipe.jpg",
      page: "recipeRecommend",
    },
    {
      id: 2,
      title: "외식, 배달",
      image: "/images/delivery.jpg",
      page: "delivery",
    },
    {
      id: 3,
      title: "수치환산",
      image: "/images/calculator.jpg",
      page: "calculator",
    },
  ];

  return (
    <div className="meal-page">
      {menuItems.map((item) => (
        <div
          className="meal-card"
          key={item.id}
          role="button"
          tabIndex={0}
          onClick={() => setCurrentPage(item.page)}
          onKeyDown={(event) => {
            if (event.key === "Enter" || event.key === " ") {
              setCurrentPage(item.page);
            }
          }}
        >
          <img
            src={item.image}
            alt={item.title}
            className="meal-image"
          />

          <div className="meal-info">
            <h2>{item.title}</h2>

            <button
              type="button"
              onClick={(event) => {
                event.stopPropagation();
                setCurrentPage(item.page);
              }}
            >
              선택
            </button>
          </div>
        </div>
      ))}
    </div>
  );
}

export default MealRecommendPage;
