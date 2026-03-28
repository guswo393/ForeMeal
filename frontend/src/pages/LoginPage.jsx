function LoginPage({ setIsLoggedIn }) {

  const handleLogin = () => {
    setIsLoggedIn(true)
  }

  return (
    <div className="login-page">

      <h1>혈당 식단관리 앱</h1>

      <input
        type="text"
        placeholder="아이디"
        className="input"
      />

      <input
        type="password"
        placeholder="비밀번호"
        className="input"
      />

      <button className="main-btn" onClick={handleLogin}>
        로그인
      </button>

    </div>
  )
}

export default LoginPage