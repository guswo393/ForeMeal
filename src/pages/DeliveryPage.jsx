import { useEffect, useState } from "react";
import "../styles/DeliveryPage.css";

function DeliveryPage() {
  const [location, setLocation] = useState("세종시 조치원읍");
  const [places, setPlaces] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchDeliveryPlaces();
  }, []);

  const fetchDeliveryPlaces = async () => {
    try {
      setLoading(true);

      // 나중에 백엔드 연결 시 이 부분 사용
      // const response = await fetch(
      //   `http://localhost:8080/api/restaurants?location=${location}`
      // );
      // const data = await response.json();
      // setPlaces(data);

      // 임시 데이터
      const data = [
        {
          id: 1,
          name: "샐러드박스 조치원점",
          category: "샐러드",
          rating: 4.7,
          reviewCount: 128,
          address: "세종 조치원읍",
          imageUrl: "/images/place.jpg",
          recommendedMenu: "닭가슴살 샐러드",
        },
        {
          id: 2,
          name: "건강한 한끼",
          category: "한식",
          rating: 4.5,
          reviewCount: 96,
          address: "세종 조치원읍",
          imageUrl: "/images/place.jpg",
          recommendedMenu: "현미 비빔밥",
        },
      ];

      setPlaces(data);
    } catch (error) {
      console.error("외식/배달 목록 불러오기 실패:", error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="delivery-page">
      <div className="delivery-search-box">
        <span className="search-icon">○</span>
        <span className="search-text">{location}</span>
        <button className="edit-btn">✎</button>
      </div>

      <div className="delivery-filter-row">
        <div>
          <button className="filter-btn">필터⌄</button>
          <button className="filter-btn">정렬⌄</button>
        </div>
        <span className="result-count">결과 {places.length}개</span>
      </div>

      <div className="map-area">
        <img src="/images/map.jpg" alt="지도" />
      </div>

      <div className="place-section">
        {loading ? (
          <p>외식/배달 추천 목록을 불러오는 중...</p>
        ) : (
          places.map((place) => (
            <div className="place-card" key={place.id}>
              <img
                src={place.imageUrl}
                alt={place.name}
                className="place-image"
              />

              <h2>{place.name}</h2>

              <p className="place-category">{place.category}</p>

              <p className="place-rating">
                ☆ {place.rating} 리뷰 {place.reviewCount}개
              </p>

              <p className="place-address">{place.address}</p>

              <p className="recommend-menu">
                추천 메뉴: {place.recommendedMenu}
              </p>

              <button className="select-btn">선택</button>
            </div>
          ))
        )}
      </div>
    </div>
  );
}

export default DeliveryPage;