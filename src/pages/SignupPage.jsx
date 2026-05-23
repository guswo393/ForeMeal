import { useState } from 'react'
import '../styles/SignupPage.css'

function SignupPage({ setAuthPage, signupUser, setSignupUser }) {
  const [form, setForm] = useState({
    userId: signupUser.userId || '',
    password: signupUser.password || '',
    passwordCheck: '',
    nickname: signupUser.nickname || '',
    weight: signupUser.weight || '',
    height: signupUser.height || '',
    birth: signupUser.birth || ''
  })

  const handleChange = (e) => {
    const { name, value } = e.target

    setForm((prev) => ({
      ...prev,
      [name]: value
    }))
  }

  const handleSignup = async () => {
    const { userId, password, passwordCheck, nickname, weight, height, birth } = form

    if (!userId || !password || !passwordCheck || !nickname || !weight || !height || !birth) {
      alert('모든 항목을 입력해주세요.')
      return
    }

    if (password !== passwordCheck) {
      alert('비밀번호가 일치하지 않습니다.')
      return
    }

    const today = new Date().toISOString().split('T')[0]

    if (birth > today) {
      alert('생일은 오늘 이후 날짜로 설정할 수 없습니다.')
      return
    }

    try {
      const response = await fetch('/api/users', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          email: userId,
          password,
          username: nickname,
          birthDate: birth,
        }),
      })

      if (!response.ok) {
        throw new Error('signup failed')
      }

      setSignupUser({
        userId,
        password,
        nickname,
        weight,
        height,
        birth
      })

      alert('회원가입이 완료되었습니다. 로그인 해주세요.')
      setAuthPage('login')
    } catch (error) {
      console.error('회원가입 실패:', error)
      alert('회원가입에 실패했습니다. 입력값이나 서버 상태를 확인해주세요.')
    }
  }

  return (
    <div className="signup-container">
      <div className="signup-box">
        <h1 className="signup-title">회원가입</h1>
        <p className="signup-subtitle">Fore:Meal 계정을 만들어보세요</p>

        <input
          type="text"
          name="userId"
          placeholder="이메일"
          value={form.userId}
          onChange={handleChange}
          className="signup-input"
        />

        <input
          type="password"
          name="password"
          placeholder="비밀번호"
          value={form.password}
          onChange={handleChange}
          className="signup-input"
        />

        <input
          type="password"
          name="passwordCheck"
          placeholder="비밀번호 확인"
          value={form.passwordCheck}
          onChange={handleChange}
          className="signup-input"
        />

        <input
          type="text"
          name="nickname"
          placeholder="닉네임"
          value={form.nickname}
          onChange={handleChange}
          className="signup-input"
        />

        <input
          type="text"
          name="weight"
          placeholder="체중"
          value={form.weight}
          onChange={handleChange}
          className="signup-input"
        />

        <input
          type="text"
          name="height"
          placeholder="키"
          value={form.height}
          onChange={handleChange}
          className="signup-input"
        />

        <input
          type="date"
          name="birth"
          value={form.birth}
          onChange={handleChange}
          max={new Date().toISOString().split('T')[0]}
          className="signup-input"
        />

        <button
          type="button"
          className="signup-submit-button"
          onClick={handleSignup}
        >
          회원가입 완료
        </button>

        <button
          type="button"
          className="signup-back-button"
          onClick={() => setAuthPage('login')}
        >
          로그인으로 돌아가기
        </button>
      </div>
    </div>
  )
}

export default SignupPage
