import { useEffect, useState } from "react";
import "../styles/RefrigeratorPage.css";

function RefrigeratorPage({ userProfile }) {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(false);
  const [form, setForm] = useState({
    inputName: "",
    quantity: "",
    unit: "g",
    storageType: "냉장",
    expirationDate: "",
    memo: "",
  });

  const authHeaders = {
    ...(userProfile?.token ? { Authorization: `Bearer ${userProfile.token}` } : {}),
  };

  const fetchItems = async () => {
    if (!userProfile?.userId || !userProfile?.token) {
      setItems([]);
      return;
    }

    try {
      setLoading(true);
      const response = await fetch(`/api/pantry/items?userId=${userProfile.userId}`, {
        headers: authHeaders,
      });

      if (!response.ok) {
        throw new Error(`HTTP ${response.status}`);
      }

      const data = await response.json();
      setItems(data);
    } catch (error) {
      console.error("냉장고 목록 불러오기 실패:", error);
      setItems([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchItems();
  }, [userProfile?.userId, userProfile?.token]);

  const handleChange = (event) => {
    const { name, value } = event.target;
    setForm((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleAdd = async (event) => {
    event.preventDefault();

    if (!form.inputName.trim()) {
      alert("재료명을 입력해주세요.");
      return;
    }

    if (!userProfile?.userId || !userProfile?.token) {
      alert("로그인 후 사용할 수 있습니다.");
      return;
    }

    try {
      const response = await fetch("/api/pantry/items", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          ...authHeaders,
        },
        body: JSON.stringify({
          userId: userProfile.userId,
          inputName: form.inputName,
          quantity: form.quantity ? Number(form.quantity) : null,
          unit: form.unit,
          storageType: form.storageType,
          expirationDate: form.expirationDate || null,
          memo: form.memo,
        }),
      });

      if (!response.ok) {
        throw new Error(`HTTP ${response.status}`);
      }

      setForm((prev) => ({
        ...prev,
        inputName: "",
        quantity: "",
        memo: "",
      }));
      fetchItems();
    } catch (error) {
      console.error("냉장고 재료 추가 실패:", error);
      alert("재료 추가에 실패했습니다.");
    }
  };

  const handleDelete = async (itemId) => {
    try {
      const response = await fetch(
        `/api/pantry/items/${itemId}?userId=${userProfile.userId}`,
        {
          method: "DELETE",
          headers: authHeaders,
        }
      );

      if (!response.ok) {
        throw new Error(`HTTP ${response.status}`);
      }

      setItems((prev) => prev.filter((item) => item.itemId !== itemId));
    } catch (error) {
      console.error("냉장고 재료 삭제 실패:", error);
      alert("재료 삭제에 실패했습니다.");
    }
  };

  return (
    <div className="refrigerator-page">
      <h2>사용자의 냉장고</h2>

      <form className="pantry-form" onSubmit={handleAdd}>
        <input
          name="inputName"
          value={form.inputName}
          onChange={handleChange}
          placeholder="재료명"
        />

        <div className="pantry-row">
          <input
            name="quantity"
            type="number"
            min="0"
            value={form.quantity}
            onChange={handleChange}
            placeholder="수량"
          />

          <select name="unit" value={form.unit} onChange={handleChange}>
            <option value="g">g</option>
            <option value="개">개</option>
            <option value="ml">ml</option>
          </select>
        </div>

        <div className="pantry-row">
          <select name="storageType" value={form.storageType} onChange={handleChange}>
            <option value="냉장">냉장</option>
            <option value="냉동">냉동</option>
            <option value="실온">실온</option>
          </select>

          <input
            name="expirationDate"
            type="date"
            value={form.expirationDate}
            onChange={handleChange}
          />
        </div>

        <input
          name="memo"
          value={form.memo}
          onChange={handleChange}
          placeholder="메모"
        />

        <button type="submit">재료 추가</button>
      </form>

      {loading ? (
        <p className="pantry-empty">냉장고를 불러오는 중...</p>
      ) : items.length ? (
        <div className="pantry-list">
          {items.map((item) => (
            <div className="pantry-card" key={item.itemId}>
              <div>
                <h3>{item.displayName}</h3>
                <p>
                  {item.quantity ?? "-"} {item.unit || ""}
                  {item.storageType && ` · ${item.storageType}`}
                </p>
                {item.expirationDate && <span>유통기한 {item.expirationDate}</span>}
              </div>

              <button type="button" onClick={() => handleDelete(item.itemId)}>
                삭제
              </button>
            </div>
          ))}
        </div>
      ) : (
        <p className="pantry-empty">등록된 재료가 없습니다.</p>
      )}
    </div>
  );
}

export default RefrigeratorPage;
