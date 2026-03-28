import { useState } from "react";
import "../styles/GlucosePage.css";

function GlucosePage() {
  const [glucose, setGlucose] = useState("");
  const [time, setTime] = useState("");
  const [mealType, setMealType] = useState("식사 전");
  const [mealHour, setMealHour] = useState("");
  const [records, setRecords] = useState([]);

  const handleMealTypeChange = (e) => {
    const selectedType = e.target.value;
    setMealType(selectedType);

    if (selectedType === "공복 혈당") {
      setMealHour("");
    }
  };

  const handleSave = () => {
    if (!glucose || !time) return;

    if (mealType !== "공복 혈당" && !mealHour) return;

    const newRecord =
      mealType === "공복 혈당"
        ? {
            time: time,
            value: glucose,
            mealType: mealType
          }
        : {
            time: time,
            value: glucose,
            mealType: mealType,
            mealHour: mealHour
          };

    setRecords([...records, newRecord]);

    setGlucose("");
    setTime("");
    setMealType("식사 전");
    setMealHour("");
  };

  return (
    <section className="page">
      <div className="card">
        <h2>혈당 기록 입력</h2>

        <input
          type="number"
          placeholder="혈당 수치를 입력하세요"
          className="input"
          value={glucose}
          onChange={(e) => setGlucose(e.target.value)}
        />

        <input
          type="time"
          className="input"
          value={time}
          onChange={(e) => setTime(e.target.value)}
        />

        <select
          className="input"
          value={mealType}
          onChange={handleMealTypeChange}
        >
          <option value="식사 전">식사 전</option>
          <option value="식사 후">식사 후</option>
          <option value="공복 혈당">공복 혈당</option>
        </select>

        {mealType !== "공복 혈당" && (
          <input
            type="number"
            placeholder="몇 시간인지 입력하세요"
            className="input"
            value={mealHour}
            onChange={(e) => setMealHour(e.target.value)}
          />
        )}

        <button className="main-btn" onClick={handleSave}>
          저장하기
        </button>
      </div>

      <div className="card">
        <h3>최근 기록</h3>

        {records.length === 0 ? (
          <p>저장된 기록이 없습니다.</p>
        ) : (
          records.map((item, index) => (
            <p key={index}>
              {item.mealType} - {item.time} - {item.value} mg/dL
              {item.mealType !== "공복 혈당" && ` (${item.mealHour}시간)`}
            </p>
          ))
        )}
      </div>
    </section>
  );
}

export default GlucosePage;