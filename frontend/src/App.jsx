import { useState } from 'react'
import Header from './components/Header'
import BottomNav from './components/BottomNav'
import HomePage from './pages/HomePage'
import GlucosePage from './pages/GlucosePage'
import MealRecommendPage from './pages/MealRecommendPage'
import MyPage from './pages/MyPage'
import LoginPage from './pages/LoginPage'

function App() {

  const [isLoggedIn, setIsLoggedIn] = useState(true)
  const [currentPage, setCurrentPage] = useState('mypage')

  const [userProfile, setUserProfile] = useState({
    userId: "plurie01",
    nickname: "플러이",
    weight: "26",
    height: "160",
    birth: "2025-04-19"
  })

  if (!isLoggedIn) {
    return <LoginPage setIsLoggedIn={setIsLoggedIn} />
  }

  const renderPage = () => {

    if (currentPage === 'home')
      return <HomePage userName={userProfile.nickname} />

    if (currentPage === 'glucose')
      return <GlucosePage />

    if (currentPage === 'meal')
      return <MealRecommendPage />

    if (currentPage === 'mypage')
      return <MyPage userProfile={userProfile} setUserProfile={setUserProfile} />

    return <HomePage userName={userProfile.nickname} />

  }

  return (
    <div className="app-container">

      <Header currentPage={currentPage} />

      <main className="main-content">
        {renderPage()}
      </main>

      <BottomNav
        currentPage={currentPage}
        setCurrentPage={setCurrentPage}
      />

    </div>
  )

}

export default App