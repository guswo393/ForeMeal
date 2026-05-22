import { useEffect, useState } from "react";
import "../styles/ConversionPage.css";

function ConversionPage({ setCurrentPage, userProfile }) {
  const [baseIngredients, setBaseIngredients] = useState([]);
  const [substituteIngredients, setSubstituteIngredients] = useState([]);

  const [baseIngredientId, setBaseIngredientId] = useState("");
  const [substituteIngredientId, setSubstituteIngredientId] = useState("");
  const [amount, setAmount] = useState("");

  const [result, setResult] = useState(null);

  useEffect(() => {
    fetchIngredients();
  }, []);

  useEffect(() => {
    if (baseIngredientId) {
      fetchSubstitutes(baseIngredientId);
    }
  }, [baseIngredientId]);

  useEffect(() => {
    if (baseIngredientId && substituteIngredientId && Number(amount) > 0) {
      calculateConversion();
    }
  }, [baseIngredientId, substituteIngredientId, amount]);

  const authHeaders = {
    ...(userProfile?.token ? { Authorization: `Bearer ${userProfile.token}` } : {}),
  };

  const fetchIngredients = async () => {
    try {
      const response = await fetch("/api/recipe/conversions/ingredients", {
        headers: authHeaders,
      });

      if (!response.ok) {
        throw new Error(`HTTP ${response.status}`);
      }

      const data = await response.json();

      setBaseIngredients(data);
      setBaseIngredientId(data[0]?.name ?? "");
    } catch (error) {
      console.error("재료 목록 불러오기 실패:", error);
    }
  };

  const fetchSubstitutes = async (ingredient) => {
    try {
      const response = await fetch(
        `/api/recipe/conversions/substitutes?ingredient=${encodeURIComponent(ingredient)}`,
        { headers: authHeaders }
      );

      if (!response.ok) {
        throw new Error(`HTTP ${response.status}`);
      }

      const data = await response.json();

      setSubstituteIngredients(data);
      setSubstituteIngredientId(data[0]?.name ?? "");
    } catch (error) {
      console.error("대체 재료 목록 불러오기 실패:", error);
      setSubstituteIngredients([]);
      setSubstituteIngredientId("");
    }
  };

  const calculateConversion = async () => {
    try {
      const response = await fetch("/api/recipe/conversions/calculate", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          ...authHeaders,
        },
        body: JSON.stringify({
          ingredient: baseIngredientId,
          substitute: substituteIngredientId,
          gram: Number(amount),
        }),
      });

      if (!response.ok) {
        throw new Error(`HTTP ${response.status}`);
      }

      const data = await response.json();
      setResult(data);
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
            <option key={item.name} value={item.name}>
              재료명 : {item.name}
            </option>
          ))}
        </select>

        <div className="amount-input">
          <input
            type="number"
            min="0"
            value={amount}
            onChange={(e) => {
              const value = e.target.value;
              setAmount(value.replace(/^0+(?=\d)/, ""));
            }}
            placeholder="g 입력"
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
            <option key={item.name} value={item.name}>
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
              {result.ingredient}{" "}
              <strong>{result.inputGramText}</strong>
            </span>

            <span className="arrow">↔</span>

            <span>
              {result.substitute}{" "}
              <strong className="green">
                {result.convertedGramText}
              </strong>
            </span>
          </div>
        </section>
      )}

      {result && (
        <section className="note-box">
          <h2>💡 참고</h2>
          <p>{result.description || result.resultText}</p>
        </section>
      )}
      
    </div>
  );
}

export default ConversionPage;
