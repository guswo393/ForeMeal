import { useEffect, useState } from "react";
import "../styles/ConversionPage.css";

function ConversionPage({ setCurrentPage }) {
  const [baseIngredients, setBaseIngredients] = useState([]);
  const [substituteIngredients, setSubstituteIngredients] = useState([]);

  const [baseIngredientId, setBaseIngredientId] = useState("");
  const [substituteIngredientId, setSubstituteIngredientId] = useState("");
  const [amount, setAmount] = useState(10);

  const [result, setResult] = useState(null);

  useEffect(() => {
    fetchIngredients();
  }, []);

  useEffect(() => {
    if (baseIngredientId && substituteIngredientId && amount) {
      calculateConversion();
    }
  }, [baseIngredientId, substituteIngredientId, amount]);

  const fetchIngredients = async () => {
    try {
      // 나중에 백엔드 연결 시 사용
      // const response = await fetch("http://localhost:8080/api/ingredients");
      // const data = await response.json();
      // setBaseIngredients(data.baseIngredients);
      // setSubstituteIngredients(data.substituteIngredients);

      // 임시 데이터
      const data = {
        baseIngredients: [
          { id: 1, name: "설탕", calories: 40, sugar: 10 },
          { id: 2, name: "꿀", calories: 30, sugar: 8 },
        ],
        substituteIngredients: [
          { id: 101, name: "스테비아", ratio: 0.3 },
          { id: 102, name: "알룰로스", ratio: 0.7 },
        ],
      };

      setBaseIngredients(data.baseIngredients);
      setSubstituteIngredients(data.substituteIngredients);

      setBaseIngredientId(data.baseIngredients[0].id);
      setSubstituteIngredientId(data.substituteIngredients[0].id);
    } catch (error) {
      console.error("재료 목록 불러오기 실패:", error);
    }
  };

  const calculateConversion = async () => {
    try {
      // 나중에 백엔드 연결 시 사용
      // const response = await fetch("http://localhost:8080/api/conversions/calculate", {
      //   method: "POST",
      //   headers: {
      //     "Content-Type": "application/json",
      //   },
      //   body: JSON.stringify({
      //     baseIngredientId,
      //     substituteIngredientId,
      //     amount,
      //   }),
      // });
      // const data = await response.json();
      // setResult(data);

      // 임시 계산
      const base = baseIngredients.find(
        (item) => String(item.id) === String(baseIngredientId)
      );

      const substitute = substituteIngredients.find(
        (item) => String(item.id) === String(substituteIngredientId)
      );

      if (!base || !substitute) return;

      const convertedAmount = Math.round(amount * substitute.ratio);

      setResult({
        baseName: base.name,
        baseAmount: amount,
        substituteName: substitute.name,
        substituteAmount: convertedAmount,
        unit: "g",
        note: `${substitute.name}는 ${base.name} 대비 약 ${
          substitute.ratio * 10
        }배 기준으로 환산됩니다.`,
      });
    } catch (error) {
      console.error("수치 환산 실패:", error);
    }
  };

  return (
    <div className="conversion-page">
      <div className="conversion-header">
        <button
          className="back-btn"
          onClick={() => setCurrentPage("meal")}
        >
          ‹
        </button>

        <div>
          <h1>수치 환산 계산기</h1>
          <p>
            기존 식재료를 대체하면 영양 수치가 어떻게 달라지는지
            확인해보세요.
          </p>
        </div>
      </div>

      <section className="conversion-box">
        <h2>기존 재료 선택</h2>

        <select
          value={baseIngredientId}
          onChange={(e) => setBaseIngredientId(e.target.value)}
        >
          {baseIngredients.map((item) => (
            <option key={item.id} value={item.id}>
              재료명 : {item.name}
            </option>
          ))}
        </select>

        <div className="amount-input">
          <input
            type="number"
            value={amount}
            onChange={(e) => setAmount(Number(e.target.value))}
          />
          <span>g</span>
        </div>
      </section>

      <section className="conversion-box">
        <h2>대체 재료 선택</h2>

        <select
          value={substituteIngredientId}
          onChange={(e) => setSubstituteIngredientId(e.target.value)}
        >
          {substituteIngredients.map((item) => (
            <option key={item.id} value={item.id}>
              {item.name}
            </option>
          ))}
        </select>
      </section>

      {result && (
        <section className="result-box">
          <h2>환산 결과</h2>

          <div className="result-row">
            <span>
              {result.baseName}{" "}
              <strong>{result.baseAmount}g</strong>
            </span>

            <span className="arrow">↔</span>

            <span>
              {result.substituteName}{" "}
              <strong className="green">
                {result.substituteAmount}g
              </strong>
            </span>
          </div>
        </section>
      )}

      {result && (
        <section className="note-box">
          <h2>💡 참고</h2>
          <p>{result.note}</p>
        </section>
      )}
      
    </div>
  );
}

export default ConversionPage;