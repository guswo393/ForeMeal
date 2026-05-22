import { useState } from 'react'
import '../styles/LoginPage.css'

function LoginPage({ setIsLoggedIn, setUserProfile, setAuthPage, signupUser }) {
  const [userId, setUserId] = useState('')
  const [password, setPassword] = useState('')

  const handleLogin = () => {
    if (!userId || !password) {
      alert('아이디와 비밀번호를 입력해주세요.')
      return
    }

    if (
      signupUser.userId &&
      userId === signupUser.userId &&
      password === signupUser.password
    ) {
      setUserProfile({
        userId: signupUser.userId,
        nickname: signupUser.nickname,
        weight: signupUser.weight,
        height: signupUser.height,
        birth: signupUser.birth
      })
      setIsLoggedIn(true)
      return
    }

    if (userId === 'plurie01' && password === '1234') {
      setUserProfile({
        userId: 'plurie01',
        nickname: '플러이',
        weight: '26',
        height: '160',
        birth: '2025-04-19'
      })
      setIsLoggedIn(true)
      return
    }

    alert('아이디 또는 비밀번호가 올바르지 않습니다.')
  }

  return (
    <div className="login-container">
      <div className="login-box">
        <h1 className="login-title">ForeMeal</h1>
        <p className="login-subtitle">맞춤 식단 관리 시작하기</p>

        <input
          type="text"
          placeholder="아이디"
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