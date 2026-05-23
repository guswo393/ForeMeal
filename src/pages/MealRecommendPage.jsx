import "../styles/MealRecommendPage.css";

function MealRecommendPage({ setCurrentPage, setRecipeRecommendType }) {
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

  const openPage = (item) => {
    if (item.page === "recipeRecommend") {
      setRecipeRecommendType("health");
    }

    setCurrentPage(item.page);
  };

  return (
    <div className="meal-page">
      {menuItems.map((item) => (
        <div
          className="meal-card"
          key={item.id}
          role="button"
          tabIndex={0}
          onClick={() => openPage(item)}
          onKeyDown={(event) => {
            if (event.key === "Enter" || event.key === " ") {
              openPage(item);
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
                openPage(item);
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
