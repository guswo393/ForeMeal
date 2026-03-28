function BottomNav({ currentPage, setCurrentPage }) {
  return (
    <nav className="bottom-nav">
      <button
        className={currentPage === 'home' ? 'nav-btn active' : 'nav-btn'}
        onClick={() => setCurrentPage('home')}
      >
        홈
      </button>

      <button
        className={currentPage === 'glucose' ? 'nav-btn active' : 'nav-btn'}
        onClick={() => setCurrentPage('glucose')}
      >
        혈당
      </button>

      <button
        className={currentPage === 'meal' ? 'nav-btn active' : 'nav-btn'}
        onClick={() => setCurrentPage('meal')}
      >
        식단추천
      </button>

      <button
        className={currentPage === 'mypage' ? 'nav-btn active' : 'nav-btn'}
        onClick={() => setCurrentPage('mypage')}
      >
        마이페이지
      </button>
    </nav>
  )
}

export default BottomNav