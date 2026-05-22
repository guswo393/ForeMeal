import { useEffect, useState } from "react";
import "../styles/GlucosePage.css";

function GlucosePage() {
  const [selectedTab, setSelectedTab] = useState("daily");
  const [predictionData, setPredictionData] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchPredictionData = async () => {
      try {
        setLoading(true);

        let mockData = null;

        if (selectedTab === "daily") {
          mockData = {
            summary: {
              glucose: {
                title: "예측 혈당",
                value: 156,
                unit: "mg/dl",
                changeText: "전일 대비 +20%",
              },
              bloodPressure: {
                title: "예측 혈압",
                value: "70/110",
                unit: "",
                changeText: "전일 대비 -13%",
              },
            },
            charts: [
              {
                id: "glucose-daily",
                title: "오늘의 혈당",
                labels: ["00", "03", "06", "09", "12", "15", "18", "21"],
                values: [30, 35, 50, 70, 90, 120, 110, 160],
              },
              {
                id: "pressure-daily",
                title: "오늘의 혈압",
                labels: ["00", "03", "06", "09", "12", "15", "18", "21"],
                values: [40, 45, 55, 60, 70, 85, 80, 120],
              },
            ],
          };
        }

        if (selectedTab === "total") {
          mockData = {
            summary: {
              glucose: {
                title: "누적 평균 혈당",
                value: 142,
                unit: "mg/dl",
                changeText: "최근 7일 평균",
              },
              bloodPressure: {
                title: "누적 평균 혈압",
                value: "75/115",
                unit: "",
                changeText: "최근 7일 평균",
              },
            },
            charts: [
              {
                id: "glucose-total",
                title: "누적 혈당",
                labels: ["월", "화", "수", "목", "금", "토", "일"],
                values: [130, 145, 138, 150, 142, 155, 148],
              },
              {
                id: "pressure-total",
                title: "누적 혈압",
                labels: ["월", "화", "수", "목", "금", "토", "일"],
                values: [110, 115, 112, 118, 116, 120, 115],
              },
            ],
          };
        }

        setPredictionData(mockData);

        // 나중에 백엔드 연결 시 위 mockData 부분 삭제하고 아래 사용
        // const response = await fetch(`/api/glucose/prediction?type=${selectedTab}`);
        // const data = await response.json();
        // setPredictionData(data);
      } catch (error) {
        console.error("예측 데이터 불러오기 실패:", error);
      } finally {
        setLoading(false);
      }
    };

    if (selectedTab !== "input") {
      fetchPredictionData();
    } else {
      setLoading(false);
      setPredictionData(null);
    }
  }, [selectedTab]);

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
      </div>

      {selectedTab === "input" ? (
        <GlucoseInputForm />
      ) : loading ? (
        <div>불러오는 중...</div>
      ) : !predictionData ? (
        <div>데이터를 불러오지 못했습니다.</div>
      ) : (
        <>
          <div className="summary-row">
            <SummaryCard data={predictionData.summary.glucose} />
            <SummaryCard data={predictionData.summary.bloodPressure} />
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
  return (
    <div className="chart-card">
      <h3>{chart.title}</h3>

      <div className="chart-area">
        {chart.values.map((value, index) => (
          <div className="chart-item" key={index}>
            <div
              className="chart-bar"
              style={{
                height: `${value}px`,
              }}
            />

            <span>{chart.labels[index]}</span>
          </div>
        ))}
      </div>
    </div>
  );
}

function GlucoseInputForm() {
  const [glucose, setGlucose] = useState("");
  const [time, setTime] = useState("");
  const [mealType, setMealType] = useState("식사 전");
  const [mealHour, setMealHour] = useState("");

  const handleSubmit = (e) => {
    e.preventDefault();

    const inputData = {
      glucose,
      time,
      mealType,
      mealHour,
    };

    console.log("입력 데이터:", inputData);

    // 나중에 백엔드 저장 연결
    // fetch("/api/glucose/records", {
    //   method: "POST",
    //   headers: {
    //     "Content-Type": "application/json",
    //   },
    //   body: JSON.stringify(inputData),
    // });
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

      <button type="submit">저장</button>
    </form>
  );
}

export default GlucosePage;