function HomePage({ userName }) {
  return (
    <section className="page">

      <div className="card">
        <h2>{userName}님의 건강 요약</h2>
        <p>오늘 혈당 상태와 추천 식단을 확인하세요.</p>
      </div>

    </section>
  )
}

export default HomePage