import { useEffect, useRef, useState } from "react";
import "../styles/RefrigeratorPage.css";

function RefrigeratorPage({ userProfile }) {
  const [items, setItems] = useState([]);
  const [scanItems, setScanItems] = useState([]);
  const [scanLoading, setScanLoading] = useState(false);
  const [loading, setLoading] = useState(false);
  const cameraInputRef = useRef(null);
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

  const readFileAsDataUrl = (file) =>
    new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onload = () => resolve(reader.result);
      reader.onerror = reject;
      reader.readAsDataURL(file);
    });

  const handleCameraClick = () => {
    if (!userProfile?.userId || !userProfile?.token) {
      alert("로그인 후 사용할 수 있습니다.");
      return;
    }

    cameraInputRef.current?.click();
  };

  const handleImageCapture = async (event) => {
    const file = event.target.files?.[0];
    event.target.value = "";

    if (!file) return;

    try {
      setScanLoading(true);
      const imageUrl = await readFileAsDataUrl(file);

      const response = await fetch("/api/pantry/scans", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          ...authHeaders,
        },
        body: JSON.stringify({
          userId: userProfile.userId,
          imageUrl,
        }),
      });

      if (!response.ok) {
        throw new Error(`HTTP ${response.status}`);
      }

      const data = await response.json();
      setScanItems(
        data.map((item) => ({
          ...item,
          selected: true,
          inputName: item.displayName || item.detectedName,
          quantity: "",
          unit: "개",
          storageType: "냉장",
          expirationDate: "",
          memo: "",
        }))
      );
    } catch (error) {
      console.error("냉장고 이미지 인식 실패:", error);
      alert("이미지 인식에 실패했습니다. AI 서버 상태를 확인해주세요.");
    } finally {
      setScanLoading(false);
    }
  };

  const updateScanItem = (index, field, value) => {
    setScanItems((prev) =>
      prev.map((item, itemIndex) =>
        itemIndex === index
          ? {
              ...item,
              [field]: value,
            }
          : item
      )
    );
  };

  const handleConfirmScan = async () => {
    const selectedItems = scanItems.filter((item) => item.selected);

    if (!selectedItems.length) {
      alert("저장할 재료를 선택해주세요.");
      return;
    }

    const scanId = selectedItems[0].scanId;

    try {
      const response = await fetch(`/api/pantry/scans/${scanId}/confirm`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          ...authHeaders,
        },
        body: JSON.stringify({
          userId: userProfile.userId,
          items: selectedItems.map((item) => ({
            selected: item.selected,
            detectedName: item.detectedName,
            inputName: item.inputName,
            foodId: item.foodId,
            confidence: item.confidence,
            quantity: item.quantity ? Number(item.quantity) : null,
            unit: item.unit,
            expirationDate: item.expirationDate || null,
            storageType: item.storageType,
            memo: item.memo,
          })),
        }),
      });

      if (!response.ok) {
        throw new Error(`HTTP ${response.status}`);
      }

      setScanItems([]);
      fetchItems();
      alert("인식된 재료를 냉장고에 저장했습니다.");
    } catch (error) {
      console.error("인식 결과 저장 실패:", error);
      alert("인식 결과 저장에 실패했습니다.");
    }
  };

  return (
    <div className="refrigerator-page">
      <div className="refrigerator-header">
        <h2>{userProfile?.nickname || "사용자"}님의 냉장고</h2>
        <button type="button" className="camera-btn" onClick={handleCameraClick}>
          📷 기록
        </button>
        <input
          ref={cameraInputRef}
          className="camera-input"
          type="file"
          accept="image/*"
          capture="environment"
          onChange={handleImageCapture}
        />
      </div>

      {scanLoading && <p className="pantry-empty">이미지를 인식하는 중...</p>}

      {scanItems.length > 0 && (
        <section className="scan-panel">
          <div className="scan-panel-header">
            <h3>인식 결과</h3>
            <button type="button" onClick={() => setScanItems([])}>
              닫기
            </button>
          </div>

          {scanItems.map((item, index) => (
            <div className="scan-item" key={`${item.scanId}-${index}`}>
              <label className="scan-check">
                <input
                  type="checkbox"
                  checked={item.selected}
                  onChange={(event) => updateScanItem(index, "selected", event.target.checked)}
                />
                <span>{item.detectedName}</span>
                <em>{Math.round((item.confidence || 0) * 100)}%</em>
              </label>

              <input
                value={item.inputName}
                onChange={(event) => updateScanItem(index, "inputName", event.target.value)}
                placeholder="저장할 재료명"
              />

              <div className="pantry-row">
                <input
                  type="number"
                  min="0"
                  value={item.quantity}
                  onChange={(event) => updateScanItem(index, "quantity", event.target.value)}
                  placeholder="수량"
                />

                <select
                  value={item.unit}
                  onChange={(event) => updateScanItem(index, "unit", event.target.value)}
                >
                  <option value="개">개</option>
                  <option value="g">g</option>
                  <option value="ml">ml</option>
                </select>
              </div>
            </div>
          ))}

          <button type="button" className="scan-save-btn" onClick={handleConfirmScan}>
            선택한 재료 저장
          </button>
        </section>
      )}

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
