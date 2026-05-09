function Header({ currentPage }) {
  const getTitle = () => {
    if (currentPage === 'home') return '혈당 식단관리 앱'
    if (currentPage === 'glucose') return '혈당 기록'
    if (currentPage === 'meal') return '식단 추천'
    if (currentPage === 'mypage') return '마이페이지'
    return '혈당 식단관리 앱'
  }

  return (
    <header className="header">
      <h1>{getTitle()}</h1>
    </header>
  )
}

export default Header