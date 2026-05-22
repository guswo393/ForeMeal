import { useState } from 'react'
import Header from './components/Header'
import BottomNav from './components/BottomNav'
import HomePage from './pages/HomePage'
import GlucosePage from './pages/GlucosePage'
import MealRecommendPage from './pages/MealRecommendPage'
import RefrigeratorPage from './pages/RefrigeratorPage'
import MyPage from './pages/MyPage'
import LoginPage from './pages/LoginPage'
import SignupPage from './pages/SignupPage'
import RecipeRecommendPage from "./pages/RecipeRecommendPage";
import DeliveryPage from './pages/DeliveryPage'
import ConversionPage from './pages/ConversionPage'

function App() {
  const [isLoggedIn, setIsLoggedIn] = useState(false)
  const [authPage, setAuthPage] = useState('login')
  const [currentPage, setCurrentPage] = useState('home')

  const [userProfile, setUserProfile] = useState({
    userId: '',
    email: '',
    nickname: '',
    weight: '',
    height: '',
    birth: '',
    token: ''
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
      return (
        <HomePage
          userName={userProfile.nickname}
          userProfile={userProfile}
          setCurrentPage={setCurrentPage}
        />
      )
    }

    if (currentPage === 'glucose') {
      return <GlucosePage userProfile={userProfile} />
    }

    if (currentPage === 'meal') {
      return <MealRecommendPage setCurrentPage={setCurrentPage} />
    }

    if (currentPage === 'recipeRecommend') {
      return <RecipeRecommendPage userProfile={userProfile} />
    }

    if (currentPage === 'delivery') {
      return <DeliveryPage userProfile={userProfile} />
    }

    if (currentPage === 'calculator') {
      return <ConversionPage setCurrentPage={setCurrentPage} userProfile={userProfile} />
    }

    if (currentPage === 'refrigerator') {
      return <RefrigeratorPage userProfile={userProfile} />
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
