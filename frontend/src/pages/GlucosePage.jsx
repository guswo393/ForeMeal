function GlucosePage() {
  return (
    <section className="page">
      <div className="card">
        <h2>혈당 기록 입력</h2>
        <input type="number" placeholder="혈당 수치를 입력하세요" className="input" />
        <input type="time" className="input" />
        <button className="main-btn">저장하기</button>
      </div>

      <div className="card">
        <h3>최근 기록</h3>
        <p>08:00 - 110 mg/dL</p>
        <p>12:30 - 135 mg/dL</p>
        <p>18:40 - 128 mg/dL</p>
      </div>
    </section>
  )
}

export default GlucosePage