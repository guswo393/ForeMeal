import { useEffect, useState } from "react";
import "../styles/MyPage.css";

function MyPage({ userProfile, setUserProfile }) {

  const [editNickname, setEditNickname] = useState(userProfile.nickname)
  const [editWeight, setEditWeight] = useState(userProfile.weight)
  const [editHeight, setEditHeight] = useState(userProfile.height)
  const [editBirth, setEditBirth] = useState(userProfile.birth)

  useEffect(() => {
    const fetchMyPage = async () => {
      if (!userProfile?.token) return

      try {
        const response = await fetch("/api/mypage/me", {
          headers: {
            Authorization: `Bearer ${userProfile.token}`,
          },
        })

        if (!response.ok) {
          throw new Error(`HTTP ${response.status}`)
        }

        const data = await response.json()
        const nextProfile = {
          ...userProfile,
          userId: data.userId,
          email: data.email,
          nickname: data.username,
          weight: data.weightKg ?? "",
          height: data.heightCm ?? "",
          birth: data.birthDate ?? "",
        }

        setUserProfile(nextProfile)
        setEditNickname(nextProfile.nickname)
        setEditWeight(nextProfile.weight)
        setEditHeight(nextProfile.height)
        setEditBirth(nextProfile.birth)
      } catch (error) {
        console.error("마이페이지 불러오기 실패:", error)
      }
    }

    fetchMyPage()
  }, [userProfile?.token])

  const now = new Date()
  const lastUpdate = userProfile.nicknameUpdatedAt
    ? new Date(userProfile.nicknameUpdatedAt)
    : null

  const canChangeNickname =
    !lastUpdate || now - lastUpdate > 7 * 24 * 60 * 60 * 1000

  const saveProfile = async (nextProfile) => {
    if (!userProfile?.token) {
      setUserProfile(nextProfile)
      return true
    }

    try {
      const response = await fetch("/api/mypage/me", {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${userProfile.token}`,
        },
        body: JSON.stringify({
          username: nextProfile.nickname,
          birthDate: nextProfile.birth,
          heightCm: nextProfile.height ? Number(nextProfile.height) : null,
          weightKg: nextProfile.weight ? Number(nextProfile.weight) : null,
        }),
      })

      if (!response.ok) {
        throw new Error(`HTTP ${response.status}`)
      }

      const data = await response.json()
      setUserProfile({
        ...nextProfile,
        userId: data.userId,
        email: data.email,
        nickname: data.username,
        birth: data.birthDate ?? "",
        height: data.heightCm ?? "",
        weight: data.weightKg ?? "",
      })
      return true
    } catch (error) {
      console.error("프로필 저장 실패:", error)
      alert("프로필 저장에 실패했습니다.")
      return false
    }
  }

  const handleSaveNickname = async () => {

    if (!canChangeNickname) {
      alert("닉네임은 7일에 한 번만 변경할 수 있습니다.")
      return
    }

    const saved = await saveProfile({
      ...userProfile,
      nickname: editNickname,
      weight: editWeight,
      height: editHeight,
      birth: editBirth,
      nicknameUpdatedAt: new Date()
    })

    if (saved) {
      alert("닉네임이 저장되었습니다.")
    }
  }

  const handleSaveProfile = async () => {

    if (!editBirth) {
      alert("생년월일을 입력해주세요.")
      return
    }

    const saved = await saveProfile({
      ...userProfile,
      nickname: editNickname,
      weight: editWeight,
      height: editHeight,
      birth: editBirth
    })

    if (saved) {
      alert("프로필이 저장되었습니다.")
    }
  }

  return (
    <div className="mypage-wrapper">

      <div className="mypage-phone">

        <div className="mypage-header">
          <h1>마이 페이지</h1>
        </div>

        <div className="profile-top-section">

          <div className="profile-icon-box">
            <div className="profile-icon-head"></div>
            <div className="profile-icon-body"></div>
          </div>

          <div className="profile-name-box">

            <div className="profile-id-line">
              ID | {userProfile.userId}
            </div>

            <div className="profile-nickname-line">
              닉네임 | {userProfile.nickname}
            </div>

            <div className="nickname-row">

              <input
                type="text"
                value={editNickname}
                onChange={(e)=>setEditNickname(e.target.value)}
                className="nickname-input"
                disabled={!canChangeNickname}
              />

              <button
                className="nickname-save-btn"
                onClick={handleSaveNickname}
                disabled={!canChangeNickname}
              >
                저장
              </button>

            </div>

            {!canChangeNickname && (
              <p className="nickname-lock-text">
                닉네임은 7일에 한 번만 변경할 수 있습니다.
              </p>
            )}

          </div>

        </div>

        <div className="profile-edit-card">

          <h2>프로필 수정</h2>
          <div className="section-line"></div>

          <div className="info-group">

            <div className="label-box">
              체중 & 신장
            </div>

            <div className="weight-height-row">

              <div className="input-unit-box">
                <input
                  type="number"
                  value={editWeight}
                  onChange={(e)=>setEditWeight(e.target.value)}
                  className="profile-input"
                />
                <span className="unit-text">kg</span>
              </div>

              <div className="input-unit-box">
                <input
                  type="number"
                  value={editHeight}
                  onChange={(e)=>setEditHeight(e.target.value)}
                  className="profile-input"
                />
                <span className="unit-text">cm</span>
              </div>

            </div>

          </div>

          <div className="info-group">

            <div className="label-box">
              생년월일
            </div>

            <input
              type="date"
              value={editBirth}
              onChange={(e)=>setEditBirth(e.target.value)}
              className="birth-input"
            />

          </div>

          <button
            className="main-save-btn"
            onClick={handleSaveProfile}
          >
            프로필 저장
          </button>

        </div>

      </div>

    </div>
  )

}

export default MyPage
