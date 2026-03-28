import { useState } from 'react'
import Header from './components/Header'
import BottomNav from './components/BottomNav'
import HomePage from './pages/HomePage'
import GlucosePage from './pages/GlucosePage'
import MealRecommendPage from './pages/MealRecommendPage'
import MyPage from './pages/MyPage'
import LoginPage from './pages/LoginPage'
import SignupPage from './pages/SignupPage'

function App() {
  const [isLoggedIn, setIsLoggedIn] = useState(false)
  const [authPage, setAuthPage] = useState('login')
  const [currentPage, setCurrentPage] = useState('home')

  const [userProfile, setUserProfile] = useState({
    userId: '',
    nickname: '',
    weight: '',
    height: '',
    birth: ''
  })

  const [signupUser, setSignupUser] = useState({
    userId: '',
    password: '',
    nickname: '',
    weight: '',
    height: '',
    birth: ''
  })

  if (!isLoggedIn) {
    if (authPage === 'signup') {
      return (
        <SignupPage
          setAuthPage={setAuthPage}
          signupUser={signupUser}
          setSignupUser={setSignupUser}
        />
      )
    }

    return (
      <LoginPage
        setIsLoggedIn={setIsLoggedIn}
        setUserProfile={setUserProfile}
        setAuthPage={setAuthPage}
        signupUser={signupUser}
      />
    )
  }

  const renderPage = () => {
    if (currentPage === 'home') {
      return <HomePage userName={userProfile.nickname} />
    }

    if (currentPage === 'glucose') {
      return <GlucosePage />
    }

    if (currentPage === 'meal') {
      return <MealRecommendPage />
    }

    if (currentPage === 'mypage') {
      return (
        <MyPage
          userProfile={userProfile}
          setUserProfile={setUserProfile}
        />
      )
    }

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