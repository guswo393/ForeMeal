import { useState } from 'react'
import '../styles/LoginPage.css'

function LoginPage({ setIsLoggedIn, setUserProfile, setAuthPage, signupUser }) {
  const [userId, setUserId] = useState('')
  const [password, setPassword] = useState('')

  const handleLogin = async () => {
    if (!userId || !password) {
      alert('이메일과 비밀번호를 입력해주세요.')
      return
    }

    try {
      const response = await fetch('/api/auth/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          email: userId,
          password,
        }),
      })

      if (!response.ok) {
        throw new Error('login failed')
      }

      const data = await response.json()

      setUserProfile({
        userId: data.userId,
        email: data.email,
        nickname: data.username,
        weight: '',
        height: '',
        birth: '',
        token: data.accessToken
      })
      setIsLoggedIn(true)
      return
    } catch (error) {
      console.error('로그인 실패:', error)
    }

    if (
      signupUser.userId &&
      userId === signupUser.userId &&
      password === signupUser.password
    ) {
      setUserProfile({
        userId: signupUser.userId,
        email: signupUser.userId,
        nickname: signupUser.nickname,
        weight: signupUser.weight,
        height: signupUser.height,
        birth: signupUser.birth,
        token: ''
      })
      setIsLoggedIn(true)
      return
    }

    alert('아이디 또는 비밀번호가 올바르지 않습니다.')
  }

  return (
    <div className="login-container">
      <div className="login-box">
        <h1 className="login-title">Fore:Meal</h1>
        <p className="login-subtitle">맞춤 식단 관리 시작하기</p>

        <input
          type="text"
          placeholder="이메일"
          value={userId}
          onChange={(e) => setUserId(e.target.value)}
          className="login-input"
        />

        <input
          type="password"
          placeholder="비밀번호"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          className="login-input"
        />

        <button type="button" className="login-button" onClick={handleLogin}>
          로그인
        </button>

        <button
          type="button"
          className="signup-button"
          onClick={() => setAuthPage('signup')}
        >
          회원가입
        </button>
      </div>
    </div>
  )
}

export default LoginPage
