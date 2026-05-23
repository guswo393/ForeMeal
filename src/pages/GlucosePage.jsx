import { useEffect, useState } from "react";
import "../styles/GlucosePage.css";

function GlucosePage({ userProfile, predictionFood, clearPredictionFood }) {
  const [selectedTab, setSelectedTab] = useState("input");
  const [predictionData, setPredictionData] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (predictionFood) {
      setSelectedTab("predict");
    }
  }, [predictionFood]);

  useEffect(() => {
    const fetchPredictionData = async () => {
      try {
        setLoading(true);

        if (!userProfile?.token) {
          setPredictionData(null);
          return;
        }

        const today = new Date().toISOString().split("T")[0];
        const response = await fetch(`/api/glucose/daily?date=${today}`, {
          headers: {
            Authorization: `Bearer ${userProfile.token}`,
          },
        });

        if (!response.ok) {
          throw new Error(`HTTP ${response.status}`);
        }

        const data = await response.json();
        const records = data.records ?? [];
        const values = records.map((record) => Number(record.glucoseValue));
        const latest = records[records.length - 1];
        const average = values.length
          ? Math.round(values.reduce((sum, value) => sum + value, 0) / values.length)
          : null;

        setPredictionData({
          summary: {
            glucose: {
              title: selectedTab === "daily" ? "최근 혈당" : "오늘 평균 혈당",
              value: selectedTab === "daily" ? latest?.glucoseValue ?? "-" : average ?? "-",
              unit: selectedTab === "daily" && !latest ? "" : "mg/dl",
              changeText: values.length ? `${values.length}건 기록됨` : "아직 입력된 기록이 없습니다",
            },
          },
          charts: [
            {
              id: "glucose-daily",
              title: selectedTab === "daily" ? "오늘의 혈당" : "오늘 혈당 기록",
              labels: records.map((record) => record.measuredAt.slice(11, 16)),
              values,
            },
          ],
        });
      } catch (error) {
        console.error("예측 데이터 불러오기 실패:", error);
        setPredictionData(null);
      } finally {
        setLoading(false);
      }
    };

    if (selectedTab === "daily" || selectedTab === "total") {
      fetchPredictionData();
    } else {
      setLoading(false);
      setPredictionData(null);
    }
  }, [selectedTab, userProfile?.token]);

  return (
    <div className="glucose-page">
      <header className="glucose-header">
        <h2>혈당 예측</h2>
      </header>

      <div className="glucose-tabs">
        <button
          className={selectedTab === "daily" ? "active" : ""}
          onClick={() => setSelectedTab("daily")}
        >
          일일
        </button>

        <button
          className={selectedTab === "total" ? "active" : ""}
          onClick={() => setSelectedTab("total")}
        >
          누적
        </button>

        <button
          className={selectedTab === "input" ? "active" : ""}
          onClick={() => setSelectedTab("input")}
        >
          입력
        </button>

        <button
          className={selectedTab === "predict" ? "active" : ""}
          onClick={() => setSelectedTab("predict")}
        >
          예측
        </button>
      </div>

      {selectedTab === "input" ? (
        <GlucoseInputForm userProfile={userProfile} />
      ) : selectedTab === "predict" ? (
        <GlucosePredictionForm
          userProfile={userProfile}
          predictionFood={predictionFood}
          clearPredictionFood={clearPredictionFood}
        />
      ) : loading ? (
        <div>불러오는 중...</div>
      ) : !predictionData ? (
        <div>데이터를 불러오지 못했습니다.</div>
      ) : (
        <>
          <div className="summary-row">
            <SummaryCard data={predictionData.summary.glucose} />
          </div>

          {predictionData.charts.map((chart) => (
            <SimpleChart key={chart.id} chart={chart} />
          ))}
        </>
      )}
    </div>
  );
}

function SummaryCard({ data }) {
  return (
    <div className="summary-card">
      <p>{data.title}</p>

      <h1>
        {data.value} {data.unit}
      </h1>

      <span>{data.changeText}</span>
    </div>
  );
}

function SimpleChart({ chart }) {
  if (!chart.values.length) {
    return (
      <div className="chart-card">
        <h3>{chart.title}</h3>
        <div>오늘 입력된 혈당 기록이 없습니다.</div>
      </div>
    );
  }

  return (
    <div className="chart-card">
      <h3>{chart.title}</h3>

      <div className="chart-area">
        {chart.values.map((value, index) => (
          <div className="chart-item" key={index}>
            <div
              className="chart-bar"
              style={{
                height: `${Math.min(value, 180)}px`,
              }}
            />

            <span>{chart.labels[index]}</span>
          </div>
        ))}
      </div>
    </div>
  );
}

function GlucoseInputForm({ userProfile }) {
  const [glucose, setGlucose] = useState("");
  const [time, setTime] = useState("");
  const [mealType, setMealType] = useState("식사 전");
  const [mealHour, setMealHour] = useState("");

  const measureTypeMap = {
    "식사 전": "BEFORE_MEAL",
    "식사 후": mealHour === "1" ? "AFTER_MEAL_1H" : "AFTER_MEAL_2H",
    "공복 혈당": "FASTING",
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!userProfile?.token) {
      alert("로그인 토큰이 없어 저장할 수 없습니다. 다시 로그인해주세요.");
      return;
    }

    if (!glucose || !time) {
      alert("혈당과 시간을 입력해주세요.");
      return;
    }

    const today = new Date().toISOString().split("T")[0];
    const inputData = {
      measuredAt: `${today}T${time}:00`,
      glucoseValue: Number(glucose),
      measureType: measureTypeMap[mealType],
      memo: mealType === "식사 후" && mealHour ? `식사 ${mealHour}시간 후` : mealType,
    };

    try {
      const response = await fetch("/api/glucose", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${userProfile.token}`,
        },
        body: JSON.stringify(inputData),
      });

      if (!response.ok) {
        throw new Error(`HTTP ${response.status}`);
      }

      alert("혈당 기록이 저장되었습니다.");
      setGlucose("");
      setTime("");
      setMealHour("");
    } catch (error) {
      console.error("혈당 저장 실패:", error);
      alert("혈당 저장에 실패했습니다.");
    }
  };

  return (
    <form className="glucose-input-form" onSubmit={handleSubmit}>
      <label>
        혈당
        <input
          type="number"
          value={glucose}
          onChange={(e) => setGlucose(e.target.value)}
          placeholder="혈당 수치 입력"
        />
      </label>

      <label>
        시간
        <input
          type="time"
          value={time}
          onChange={(e) => setTime(e.target.value)}
        />
      </label>

      <label>
        식사 상태
        <select
          value={mealType}
          onChange={(e) => {
            setMealType(e.target.value);

            if (e.target.value === "공복 혈당") {
              setMealHour("");
            }
          }}
        >
          <option>식사 전</option>
          <option>식사 후</option>
          <option>공복 혈당</option>
        </select>
      </label>

      {mealType !== "공복 혈당" && (
        <label>
          식사 시간
          <input
            type="number"
            value={mealHour}
            onChange={(e) => setMealHour(e.target.value)}
            placeholder="몇 시간 전/후"
          />
        </label>
      )}

      <button type="submit">혈당 기록 저장</button>
    </form>
  );
}

function GlucosePredictionForm({ userProfile, predictionFood, clearPredictionFood }) {
  const [selectedFood, setSelectedFood] = useState(predictionFood);
  const [foodQuery, setFoodQuery] = useState("");
  const [foodResults, setFoodResults] = useState([]);
  const [searching, setSearching] = useState(false);
  const [foodSearched, setFoodSearched] = useState(false);
  const [glucose, setGlucose] = useState("");
  const [activityLevel, setActivityLevel] = useState("normal");
  const [time, setTime] = useState("");
  const [mealType, setMealType] = useState("식사 전");
  const [mealHour, setMealHour] = useState("");
  const [predictionResult, setPredictionResult] = useState(null);
  const [predicting, setPredicting] = useState(false);

  useEffect(() => {
    setSelectedFood(predictionFood);
    setPredictionResult(null);
  }, [predictionFood]);

  const normalizeFood = (food) => ({
    foodId: food.foodId ?? null,
    recipeId: food.recipeId ?? null,
    sourceType: food.sourceType ?? "FOOD",
    name: food.name ?? food.foodName,
    calories: Number(food.calories ?? 0),
    carbs: Number(food.carbs ?? 0),
    sugar: Number(food.sugar ?? 0),
    sodium: Number(food.sodium ?? 0),
    quantity: 1,
  });

  useEffect(() => {
    const query = foodQuery.trim();
    if (!query) {
      setFoodResults([]);
      setFoodSearched(false);
      return;
    }

    const timer = setTimeout(() => {
      searchFood(query, false);
    }, 250);

    return () => clearTimeout(timer);
  }, [foodQuery]);

  const searchFood = async (query, markSearched = true) => {
    try {
      setSearching(true);
      const response = await fetch(`/api/v1/predict/foods/search?query=${encodeURIComponent(query)}`, {
        headers: userProfile?.token
          ? { Authorization: `Bearer ${userProfile.token}` }
          : {},
      });

      if (!response.ok) {
        throw new Error(`HTTP ${response.status}`);
      }

      const data = await response.json();
      setFoodResults(data.slice(0, 8));
      setFoodSearched(markSearched);
    } catch (error) {
      console.error("음식 검색 실패:", error);
      alert("음식 검색에 실패했습니다.");
    } finally {
      setSearching(false);
    }
  };

  const handleSearchFood = async (event) => {
    event.preventDefault();

    if (!foodQuery.trim()) {
      alert("검색할 음식명을 입력해주세요.");
      return;
    }

    await searchFood(foodQuery.trim(), true);
  };

  const handlePredict = async () => {
    if (!userProfile?.token) {
      alert("로그인 토큰이 없어 예측할 수 없습니다. 다시 로그인해주세요.");
      return;
    }

    if (!selectedFood) {
      alert("예측할 음식을 선택해주세요.");
      return;
    }

    try {
      setPredicting(true);
      setPredictionResult(null);

      const payload = {
        currentGlucose: glucose ? Number(glucose) : null,
        activityLevel,
        foods: [
          {
            foodId: selectedFood.foodId ?? null,
            name: selectedFood.name,
            calories: Number(selectedFood.calories ?? 0),
            carbs: Number(selectedFood.carbs ?? 0),
            sugar: Number(selectedFood.sugar ?? 0),
            sodium: Number(selectedFood.sodium ?? 0),
            quantity: 1,
          },
        ],
      };

      const response = await fetch("/api/v1/predict/glucose", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${userProfile.token}`,
        },
        body: JSON.stringify(payload),
      });

      if (!response.ok) {
        throw new Error(`HTTP ${response.status}`);
      }

      const data = await response.json();
      setPredictionResult(data);
    } catch (error) {
      console.error("혈당 예측 실패:", error);
      alert("혈당 예측에 실패했습니다. AI 서버 상태를 확인해주세요.");
    } finally {
      setPredicting(false);
    }
  };

  return (
    <div className="glucose-input-form">
      {selectedFood ? (
        <section className="selected-food-card">
          <div>
            <span>예측 음식</span>
            <h3>{selectedFood.name}</h3>
            <p>
              {selectedFood.calories ?? 0} kcal · 탄수화물 {selectedFood.carbs ?? 0}g · 당류 {selectedFood.sugar ?? 0}g · 나트륨 {selectedFood.sodium ?? 0}mg
            </p>
          </div>

          <button
            type="button"
            className="clear-food-btn"
            onClick={() => {
              setSelectedFood(null);
              clearPredictionFood?.();
              setPredictionResult(null);
            }}
          >
            지우기
          </button>
        </section>
      ) : (
        <section className="food-search-panel">
          <form className="food-search-form" onSubmit={handleSearchFood}>
            <input
              type="text"
              value={foodQuery}
              onChange={(event) => setFoodQuery(event.target.value)}
              placeholder="음식명 검색"
            />
            <button type="submit" disabled={searching}>
              {searching ? "검색 중" : "검색"}
            </button>
          </form>

          <div className="food-search-results">
            {foodResults.map((food) => (
              <button
                type="button"
                key={`${food.sourceType}-${food.sourceId}`}
                onClick={() => {
                  setSelectedFood(normalizeFood(food));
                  setFoodResults([]);
                  setFoodSearched(false);
                  setFoodQuery("");
                }}
              >
                <strong>{food.name}</strong>
                <span>
                  {food.sourceType === "RECIPE" ? "레시피" : "식품"} · {food.calories ?? 0} kcal · 탄수화물 {food.carbs ?? 0}g · 당류 {food.sugar ?? 0}g
                </span>
              </button>
            ))}

            {foodSearched && !foodResults.length && (
              <p className="food-search-empty">검색된 음식이 없습니다.</p>
            )}
          </div>
        </section>
      )}

      <label>
        혈당
        <input
          type="number"
          value={glucose}
          onChange={(e) => setGlucose(e.target.value)}
          placeholder="혈당 수치 입력"
        />
      </label>

      <label>
        활동량
        <select
          value={activityLevel}
          onChange={(e) => setActivityLevel(e.target.value)}
        >
          <option value="low">낮음</option>
          <option value="normal">보통</option>
          <option value="high">높음</option>
        </select>
      </label>

      <label>
        시간
        <input
          type="time"
          value={time}
          onChange={(e) => setTime(e.target.value)}
        />
      </label>

      <label>
        식사 상태
        <select
          value={mealType}
          onChange={(e) => {
            setMealType(e.target.value);

            if (e.target.value === "공복 혈당") {
              setMealHour("");
            }
          }}
        >
          <option>식사 전</option>
          <option>식사 후</option>
          <option>공복 혈당</option>
        </select>
      </label>

      {mealType !== "공복 혈당" && (
        <label>
          식사 시간
          <input
            type="number"
            value={mealHour}
            onChange={(e) => setMealHour(e.target.value)}
            placeholder="몇 시간 전/후"
          />
        </label>
      )}

      <button type="button" onClick={handlePredict} disabled={predicting || !selectedFood}>
        {predicting ? "예측 중..." : "식후 혈당 예측"}
      </button>

      {predictionResult && (
        <section className="prediction-result-card">
          <p>예상 최고 혈당</p>
          <h3>{Math.round(predictionResult.predictedPeak)} mg/dl</h3>
          <span>위험도 {predictionResult.riskLevel}</span>

          {predictionResult.predictionCurve?.length > 0 && (
            <div className="prediction-curve">
              {predictionResult.predictionCurve.map((point) => (
                <div className="prediction-point" key={point.minute}>
                  <strong>{Math.round(point.glucoseMgdl)}</strong>
                  <span>{point.minute}분</span>
                </div>
              ))}
            </div>
          )}
        </section>
      )}
    </div>
  );
}

export default GlucosePage;
