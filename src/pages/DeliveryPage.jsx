import { useEffect, useState } from "react";
import "../styles/DeliveryPage.css";

function DeliveryPage({ userProfile }) {
  const searchModeOptions = [
    { value: "address", label: "주소로 검색" },
    { value: "restaurant", label: "식당으로 검색" },
    { value: "current", label: "현위치" },
  ];
  const filterOptions = ["전체", "한식", "중식", "일식", "양식", "분식", "샐러드", "패스트푸드", "카페/디저트"];
  const sortOptions = ["가까운순", "주의도 낮은순", "이름순"];
  const riskOrder = {
    LOW: 1,
    CAUTION: 2,
    HIGH: 3,
  };

  const [location, setLocation] = useState("현재 위치 확인 중");
  const [searchText, setSearchText] = useState("");
  const [appliedQuery, setAppliedQuery] = useState("");
  const [searchScope, setSearchScope] = useState("nearby");
  const [searchMode, setSearchMode] = useState("address");
  const [coords, setCoords] = useState({
    lat: "36.6010",
    lng: "127.2988",
  });
  const [places, setPlaces] = useState([]);
  const [selectedFilter, setSelectedFilter] = useState("전체");
  const [selectedSort, setSelectedSort] = useState("가까운순");
  const [loading, setLoading] = useState(true);
  const [locationReady, setLocationReady] = useState(false);

  useEffect(() => {
    if (!navigator.geolocation) {
      setLocation("세종시 조치원읍");
      setLocationReady(true);
      return;
    }

    navigator.geolocation.getCurrentPosition(
      (position) => {
        setCoords({
          lat: position.coords.latitude.toFixed(6),
          lng: position.coords.longitude.toFixed(6),
        });
        setLocation("현재 위치");
        setLocationReady(true);
      },
      () => {
        setLocation("세종시 조치원읍");
        setLocationReady(true);
      }
    );
  }, []);

  useEffect(() => {
    if (!locationReady) return;
    fetchDeliveryPlaces();
  }, [userProfile?.token, coords.lat, coords.lng, appliedQuery, locationReady]);

  const fetchDeliveryPlaces = async () => {
    try {
      setLoading(true);

      if (!userProfile?.token) {
        setPlaces([]);
        return;
      }

      const queryParam = appliedQuery
        ? `query=${encodeURIComponent(appliedQuery)}`
        : "";
      const locationParams = searchScope === "nearby"
        ? `lat=${coords.lat}&lng=${coords.lng}&radius=3000`
        : "";
      const requestParams = [locationParams, queryParam]
        .filter(Boolean)
        .join("&");

      const response = await fetch(
        `/api/out-guide/restaurants?${requestParams}`,
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

      setPlaces(data.restaurants ?? []);
    } catch (error) {
      console.error("외식/배달 목록 불러오기 실패:", error);
      setPlaces([]);
    } finally {
      setLoading(false);
    }
  };

  const handleUseCurrentLocation = () => {
    if (!navigator.geolocation) {
      alert("현재 위치를 사용할 수 없습니다.");
      return;
    }

    navigator.geolocation.getCurrentPosition(
      (position) => {
        setCoords({
          lat: position.coords.latitude.toFixed(6),
          lng: position.coords.longitude.toFixed(6),
        });
        setLocation("현재 위치");
        setAppliedQuery("");
        setSearchText("");
        setSearchScope("nearby");
      },
      () => alert("위치 권한을 허용하면 주변 음식점을 조회할 수 있습니다.")
    );
  };

  const handleApplyAddress = async () => {
    const query = searchText.trim();

    if (!query) {
      alert("주소를 입력해주세요.");
      return;
    }

    if (!userProfile?.token) {
      alert("로그인 후 사용할 수 있습니다.");
      return;
    }

    try {
      const response = await fetch(
        `/api/out-guide/location?query=${encodeURIComponent(query)}`,
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

      setLocation(data.address || data.query);
      setCoords({
        lat: String(data.lat),
        lng: String(data.lng),
      });
      setSearchText("");
      setAppliedQuery("");
      setSearchScope("nearby");
    } catch (error) {
      console.error("주소 검색 실패:", error);
      alert("주소를 찾지 못했습니다.");
    }
  };

  const handleSearch = () => {
    const query = searchText.trim();

    if (!query) {
      alert("검색할 식당 이름을 입력해주세요.");
      return;
    }

    setAppliedQuery(query);
    setSearchScope("global");
  };

  const handleSubmitSearch = () => {
    if (searchMode === "current") {
      handleUseCurrentLocation();
      return;
    }

    if (searchMode === "address") {
      handleApplyAddress();
      return;
    }

    handleSearch();
  };

  const handleClearSearch = () => {
    setSearchText(location);
    setAppliedQuery("");
  };

  const getCategoryText = (place) => place.categoryDetail || place.category || "";

  const matchesFilter = (place) => {
    if (selectedFilter === "전체") return true;

    const categoryText = getCategoryText(place);

    if (selectedFilter === "카페/디저트") {
      return categoryText.includes("카페") || categoryText.includes("디저트") || categoryText.includes("베이커리");
    }

    return categoryText.includes(selectedFilter);
  };

  const filteredPlaces = places
    .filter(matchesFilter)
    .sort((a, b) => {
      if (selectedSort === "주의도 낮은순") {
        return (riskOrder[a.riskLevel] ?? 99) - (riskOrder[b.riskLevel] ?? 99);
      }

      if (selectedSort === "이름순") {
        return a.name.localeCompare(b.name, "ko");
      }

      return (a.distanceMeters ?? Number.MAX_SAFE_INTEGER) - (b.distanceMeters ?? Number.MAX_SAFE_INTEGER);
    });

  return (
    <div className="delivery-page">
      <div className="delivery-search-box">
        <span className="delivery-search-icon">⌕</span>
        <input
          className="search-text"
          value={searchText}
          onChange={(e) => setSearchText(e.target.value)}
          onKeyDown={(event) => {
            if (event.key === "Enter") {
              handleSubmitSearch();
            }
          }}
          placeholder={
            searchMode === "address"
              ? "주소 입력"
              : searchMode === "restaurant"
                ? "식당 이름 입력"
                : "현재 위치 기준"
          }
          aria-label="주소 또는 식당 이름"
          disabled={searchMode === "current"}
        />
        {appliedQuery && (
          <button className="clear-btn" type="button" onClick={handleClearSearch}>
            ×
          </button>
        )}
      </div>

      <div className="search-mode-row">
        {searchModeOptions.map((option) => (
          <button
            key={option.value}
            type="button"
            className={searchMode === option.value ? "chip active" : "chip"}
            onClick={() => {
              setSearchMode(option.value);
              setAppliedQuery("");
              if (option.value === "current") {
                handleUseCurrentLocation();
              }
            }}
          >
            {option.label}
          </button>
        ))}
      </div>

      <div className="delivery-controls">
        <div className="control-group">
          <span className="control-label">필터:</span>
          <div className="chip-row">
            {filterOptions.map((option) => (
              <button
                key={option}
                type="button"
                className={selectedFilter === option ? "chip active" : "chip"}
                onClick={() => setSelectedFilter(option)}
              >
                {option}
              </button>
            ))}
          </div>
        </div>

        <div className="control-group">
          <span className="control-label">정렬:</span>
          <div className="chip-row">
            {sortOptions.map((option) => (
              <button
                key={option}
                type="button"
                className={selectedSort === option ? "chip active" : "chip"}
                onClick={() => setSelectedSort(option)}
              >
                {option}
              </button>
            ))}
          </div>
        </div>

        <span className="result-count">결과 {filteredPlaces.length}개</span>
      </div>

      <div className="map-area">
        <ResultMap
          center={coords}
          location={searchScope === "global" && appliedQuery ? "식당명 검색" : location}
          query={appliedQuery}
          places={filteredPlaces}
          useResultBounds={searchScope === "global"}
        />
      </div>

      <div className="place-section">
        {loading ? (
          <p>외식/배달 추천 목록을 불러오는 중...</p>
        ) : !places.length || !filteredPlaces.length ? (
          <p>조회된 식당이 없습니다.</p>
        ) : (
          filteredPlaces.map((place) => (
            <div className="place-card" key={place.placeId}>
              <h2>{place.name}</h2>

              <p className="place-category">{place.categoryDetail || place.category}</p>

              <p className="place-rating">
                위험도 {place.riskLevel}
                {place.distanceMeters != null && ` · 거리 ${place.distanceMeters}m`}
              </p>

              <p className="place-address">{place.address}</p>

              <p className="recommend-menu">
                {place.riskMessage}
              </p>

              {place.riskTags?.length > 0 && (
                <div className="risk-tags">
                  {place.riskTags.map((tag) => (
                    <span key={tag}>{tag}</span>
                  ))}
                </div>
              )}

              <button
                className="select-btn"
                type="button"
                onClick={() => window.open(`https://map.kakao.com/link/search/${encodeURIComponent(place.name)}`, "_blank")}
              >
                지도
              </button>
            </div>
          ))
        )}
      </div>
    </div>
  );
}

function ResultMap({ center, location, query, places, useResultBounds }) {
  const centerLat = Number(center.lat);
  const centerLng = Number(center.lng);
  const validPlaces = places.filter((place) => place.lat && place.lng);
  const includeCenter = !useResultBounds || !validPlaces.length;
  const lats = [
    ...(includeCenter ? [centerLat] : []),
    ...validPlaces.map((place) => Number(place.lat)),
  ];
  const lngs = [
    ...(includeCenter ? [centerLng] : []),
    ...validPlaces.map((place) => Number(place.lng)),
  ];
  const minLat = Math.min(...lats) - 0.01;
  const maxLat = Math.max(...lats) + 0.01;
  const minLng = Math.min(...lngs) - 0.01;
  const maxLng = Math.max(...lngs) + 0.01;
  const markerLat = includeCenter ? centerLat : Number(validPlaces[0].lat);
  const markerLng = includeCenter ? centerLng : Number(validPlaces[0].lng);
  const mapUrl = `https://www.openstreetmap.org/export/embed.html?bbox=${minLng},${minLat},${maxLng},${maxLat}&layer=mapnik&marker=${markerLat},${markerLng}`;

  const pinStyle = (place) => {
    const lat = Number(place.lat);
    const lng = Number(place.lng);
    const left = ((lng - minLng) / (maxLng - minLng)) * 100;
    const top = ((maxLat - lat) / (maxLat - minLat)) * 100;

    return {
      left: `${Math.min(Math.max(left, 5), 95)}%`,
      top: `${Math.min(Math.max(top, 8), 92)}%`,
    };
  };

  return (
    <div className="result-map">
      <iframe
        title="검색 결과 지도"
        src={mapUrl}
        loading="lazy"
      />

      <div className="map-caption">
        <strong>{location}</strong>
        <span>
          {validPlaces.length}개 표시
          {query && ` · "${query}" 검색`}
        </span>
      </div>

      {validPlaces.slice(0, 20).map((place, index) => (
        <button
          key={place.placeId}
          type="button"
          className="map-pin"
          style={pinStyle(place)}
          title={place.name}
          onClick={() => window.open(`https://map.kakao.com/link/search/${encodeURIComponent(place.name)}`, "_blank")}
        >
          {index + 1}
        </button>
      ))}
    </div>
  );
}

export default DeliveryPage;
