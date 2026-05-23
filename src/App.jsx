import { useEffect, useState } from 'react'
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
  const [recipeRecommendType, setRecipeRecommendType] = useState('health')
  const [predictionFood, setPredictionFood] = useState(null)

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

  useEffect(() => {
    window.scrollTo({ top: 0, left: 0, behavior: 'auto' })
  }, [currentPage, recipeRecommendType])

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
          setRecipeRecommendType={setRecipeRecommendType}
        />
      )
    }

    if (currentPage === 'glucose') {
      return (
        <GlucosePage
          userProfile={userProfile}
          predictionFood={predictionFood}
          clearPredictionFood={() => setPredictionFood(null)}
        />
      )
    }

    if (currentPage === 'meal') {
      return (
        <MealRecommendPage
          setCurrentPage={setCurrentPage}
          setRecipeRecommendType={setRecipeRecommendType}
        />
      )
    }

    if (currentPage === 'recipeRecommend') {
      return (
        <RecipeRecommendPage
          userProfile={userProfile}
          recommendationType={recipeRecommendType}
          setCurrentPage={setCurrentPage}
          setPredictionFood={setPredictionFood}
        />
      )
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

    return (
      <HomePage
        userName={userProfile.nickname}
        userProfile={userProfile}
        setCurrentPage={setCurrentPage}
        setRecipeRecommendType={setRecipeRecommendType}
      />
    )
  }

  return (
    <div className="app-container">
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
