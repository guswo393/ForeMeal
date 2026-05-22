import { useEffect, useState } from "react";
import "../styles/GlucosePage.css";

function GlucosePage({ userProfile }) {
  const [selectedTab, setSelectedTab] = useState("input");
  const [predictionData, setPredictionData] = useState(null);
  const [loading, setLoading] = useState(true);

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

    if (selectedTab !== "input") {
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
      </div>

      {selectedTab === "input" ? (
        <GlucoseInputForm userProfile={userProfile} />
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

      <button type="submit">저장</button>
    </form>
  );
}

export default GlucosePage;
