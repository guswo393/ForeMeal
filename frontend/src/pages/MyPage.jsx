import { useState } from "react";
import "../styles/mypage.css";

function MyPage({ userProfile, setUserProfile }) {

  const [editNickname, setEditNickname] = useState(userProfile.nickname)
  const [editWeight, setEditWeight] = useState(userProfile.weight)
  const [editHeight, setEditHeight] = useState(userProfile.height)
  const [editBirth, setEditBirth] = useState(userProfile.birth)

  const now = new Date()
  const lastUpdate = userProfile.nicknameUpdatedAt
    ? new Date(userProfile.nicknameUpdatedAt)
    : null

  const canChangeNickname =
    !lastUpdate || now - lastUpdate > 7 * 24 * 60 * 60 * 1000

  const handleSaveNickname = () => {

    if (!canChangeNickname) {
      alert("닉네임은 7일에 한 번만 변경할 수 있습니다.")
      return
    }

    setUserProfile({
      ...userProfile,
      nickname: editNickname,
      nicknameUpdatedAt: new Date()
    })

  }

  const handleSaveProfile = () => {

    setUserProfile({
      ...userProfile,
      weight: editWeight,
      height: editHeight,
      birth: editBirth
    })

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