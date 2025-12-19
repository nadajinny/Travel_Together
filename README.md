# 🌍 Travel Together
### 함께 계획하고 함께 떠나는 협업형 여행 계획 앱

**Travel Together**는 친구, 가족, 동료 등 여러 사용자가
**여행을 함께 계획하고 준비할 수 있도록 설계된 Android 기반 협업형 여행 플랫폼**입니다.

일정 관리, 준비물 체크, 예산 정리, 참여자 초대, 실시간 채팅까지
여행 준비에 필요한 모든 과정을 **하나의 앱**에서 해결할 수 있습니다.

---

## ✨ 프로젝트 개요

여행 준비는 생각보다 복잡합니다.

- 카카오톡에서 일정 조율
- 메모 앱으로 준비물 관리
- 엑셀로 예산 정리

이처럼 여러 앱을 오가야 하는 불편함을 해결하기 위해
**Travel Together는 여행 준비의 모든 요소를 하나로 통합**했습니다.

### 핵심 목표
- 여행 준비 과정의 **흐름 통합**
- 여러 명이 함께 사용하는 **협업 중심 UX**
- 실시간 소통을 통한 빠른 의사 결정
- `tripId` 중심의 **확장 가능한 구조 설계**

---

## 🎯 주요 기능

### 🔐 1. 로그인 / 회원가입
- Firebase Authentication 기반 로그인
- 이메일/비밀번호 회원가입
- 비밀번호 재설정 메일 발송
- 사용자 정보 Firestore 저장
- ID/전화번호 기반 이메일 찾기

---

### 🧳 2. 여행(Trip) 관리
- 여행 생성 / 목록 조회
- 여행 상세 화면 진입
- 이메일 기반 참여자 추가
- 여행 삭제(문서 삭제)
- Firestore 실시간 리스너로 자동 갱신

---

### 📅 3. 일정 관리 (Timeline)
- 일정 추가 / 삭제
- 날짜(DatePicker) + 시간(TimePicker) 입력
- 날짜 기준 그룹핑된 타임라인 UI
- 실시간 동기화

---

### 🎒 4. 준비물 관리 (Prepare)
- 준비물 추가 / 삭제
- 체크 상태 업데이트
- Firestore `isChecked` 필드 기반 상태 관리
- 실시간 리스너로 목록 갱신

---

### 💰 5. 예산 관리 (Budget)
- 예산 항목 추가 / 수정 / 삭제
- 금액 입력 및 수정
- 여행별 총 예산 실시간 계산

---

### 💬 6. 실시간 채팅 (Message)
- Socket.IO 기반 실시간 채팅
- 채팅방 생성/삭제 및 목록 조회
- 메시지 즉시 렌더링
- 채팅 기록 Firestore 저장(app.js 기준)
- 사용자 이름/시간/메시지 표시
- 말풍선 UI 제공

---

## 🛠 기술 스택

### Android
- Kotlin
- AndroidX / Material Components
- Jetpack Navigation Component
- ViewBinding
- RecyclerView / Dialog / Fragment

### Firebase
- Firebase Authentication
- Cloud Firestore
- Firebase Analytics

### 실시간 채팅 서버
- Node.js
- Express
- Socket.IO
- Firebase Admin SDK

---

## 🗂 프로젝트 구조

```
Travel_Together/
├─ TravelTogether/
│  ├─ app/
│  │  ├─ src/main/java/
│  │  │  ├─ ui/
│  │  │  │  ├─ Trip            # Trip / Timeline / Prepare / Budget
│  │  │  │  └─ message         # Chat / Message
│  │  │  ├─ repository         # Firebase 연동
│  │  │  └─ data/model         # 데이터 모델 & SocketManager
│  │  ├─ res/                  # Layout / Drawable / Font
│  │  └─ google-services.json
│  └─ socket_server/
│     ├─ app.js                # 채팅 서버 (DB 연동)
│     └─ index.js              # 단순 브로드캐스트
└─ CONTRIBUTION.md
```

---

## 🧭 앱 화면 흐름

1. **LoginActivity**
2. **MainActivity**
   - Drawer + BottomNavigation + AppBar
3. **TripFragment**
   - 여행 목록 / 생성
4. **TripDetailActivity**
   - Timeline / Prepare / Budget Fragment
5. **MessageFragment**
   - 채팅방 목록
6. **ChatRoomActivity**
   - 실시간 채팅

---

## 🗃 Firestore 데이터 구조

### users
```
users/{uid}
  - userId: String
  - email: String
  - phoneNumber: String
```

### trips
```
trips/{tripId}
  - tripId: String
  - title: String
  - creatorId: String
  - createdAt: Long
  - participants: [String]

trips/{tripId}/plans/{planId}
  - planId: String
  - planName: String
  - time: Long

trips/{tripId}/prepare/{docId}
  - itemName: String
  - isChecked: Boolean

trips/{tripId}/budget/{docId}
  - name: String
  - amount: Long
```

### chat_rooms
```
chat_rooms/{roomId}
  - roomName: String
  - createdBy: String
  - createdAt: Timestamp

chat_rooms/{roomId}/messages/{docId}
  - name: String
  - script: String
  - timestamp: String
```

---

## ▶ 실행 방법

### Android 앱
1. Android Studio에서 `TravelTogether/` 폴더 열기
2. Gradle Sync
3. Firebase 프로젝트에 맞는 `google-services.json` 확인
4. `LoginActivity` 실행

> AndroidManifest에 `LoginActivity`, `MainActivity`, `ChatRoomActivity`가
> 모두 LAUNCHER로 등록되어 있어, 실행 Activity를 명시적으로 선택하는 것을 권장합니다.

---

### Socket.IO 서버
```
cd TravelTogether/socket_server
npm install
node app.js
```

#### 주의사항
- `SocketManager.SERVER_URL` 서버 IP 설정 필요
  - 위치: `TravelTogether/app/src/main/java/com/example/myapplication/data/model/SocketManager.kt`
- Firebase Admin SDK용 `ServiceKey.json` 필요(app.js 기준)
- 채팅방 생성/이전 메시지 로딩은 `app.js` 기준으로 동작
- 단순 브로드캐스트만 필요하면 `index.js` 사용 가능

---

## 🌈 Travel Together의 가치

- 여행 준비 과정 자체가 즐거워짐
- 역할 분담이 명확해져 갈등 감소
- 변경 사항을 실시간으로 공유
- 팀/동아리/학과 여행에 최적화

---

## 📌 요약

- **서비스명:** Travel Together
- **플랫폼:** Android
- **핵심 가치:** 여행 준비의 협업화
- **주요 기능:** 일정 · 준비물 · 예산 · 참여자 · 실시간 채팅
- **기술:** Kotlin · Firebase · Node.js · Socket.IO

---

## 📄 라이선스
본 프로젝트는 학습 및 포트폴리오 목적의 프로젝트입니다.
