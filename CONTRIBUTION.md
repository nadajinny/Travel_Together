# 개인 기여 요약 (Android Trip Planner 프로젝트)

## 1. 프로젝트 개요
본 프로젝트는 Firebase Firestore 기반의 여행 관리 및 채팅 Android 애플리케이션으로,
여행(Trip) 생성부터 일정(Timeline), 준비물(Prepare), 예산(Budget), 메시지(Message)까지
하나의 앱에서 통합 관리할 수 있도록 설계되었다.

본인은 **네비게이션 구조 통합, Trip 중심 화면 흐름 설계, Dialog 기반 UX, Firestore 실시간 연동, UI 디자인 정리**를 중심으로 핵심 기능 구현과 구조 안정화에 기여하였다.

---

## 2. 담당 역할 요약
- 앱 전반 **네비게이션 구조 설계 및 통합**
- Trip/Plan/Prepare/Budget **화면 흐름 재구성**
- Dialog 기반 **입력 UX 설계 및 구현**
- Firebase Firestore **실시간 데이터 동기화**
- UI 디자인 시스템 정리 및 코드 병합
- 발표 자료(PPT, 대본 일부) 정리 및 설명 담당

---

## 3. 주요 기여 내용

### 3.1 네비게이션 구조 통합
- `DrawerLayout`, `NavigationView`, `BottomNavigationView`를
  **Jetpack Navigation Component** 기반으로 통합
- `NavHostFragment`, `NavController`, `AppBarConfiguration`을 사용해
  상단바(Toolbar)·드로어·하단 탭의 동작을 표준화
- 최상위 목적지(Trip, Message) 전환 시 AppBar 상태를 일관되게 유지하도록 구성

**효과**
- 화면 전환 로직 단순화
- UI/UX 일관성 향상
- Activity 간 의존도 감소

---

### 3.2 Trip 흐름 Fragment 중심 재구성
- 기존 Activity 중심 구조를 **Fragment 중심 구조**로 리팩토링
- 화면 흐름을 다음과 같이 명확히 정의
  TripFragment (목록)
  → TripDetailActivity (컨테이너)
  → TimelineFragment / PrepareFragment / BudgetFragment
- `SharedPreferences`를 통해 로그인 사용자 정보 관리
- 사용자 참여 여행 목록을 Firestore 실시간 리스너로 수신하여 즉시 반영

---

### 3.3 Dialog 기반 입력 UX 설계
화면 전환 없이 주요 입력을 처리하도록 **Dialog 중심 UX**를 설계했다.

- 여행 추가: `dialog_add_trip`
- 일정 추가/수정: `dialog_add_time`
  DatePicker, TimePicker 포함
- 준비물 추가: `dialog_add_prepare`
- 채팅방 생성: `dialog_add_chat_room`

**효과**
- 사용자 동선 최소화
- 입력 후 즉각적인 화면 반영
- 모바일 환경에 적합한 UX 제공

---

### 3.4 Firestore 실시간 데이터 동기화
- Firestore **실시간 리스너(snapshotListener)** 기반 데이터 처리
- Trip / Plan / Prepare / Budget / Chatroom 데이터 CRUD 구현
- 목록 변경 시 UI 자동 갱신 구조 정리
- Budget 영역에서 항목 변경 시 **합계 금액 실시간 업데이트** 구현

---

### 3.5 메시지 및 채팅 UI 구현
- MessageFragment에서 채팅방 목록 실시간 로딩
- RecyclerView 기반 채팅 화면 구성
- 사용자/상대방 메시지 분리:
  `item_mychat`
  `item_youchat`
- `layout_round` 배경을 적용한 말풍선 UI 구현

---

### 3.6 UI 디자인 시스템 정리
- 그라데이션 배경(side_nav_bar) 적용
- 커스텀 폰트 도입 및 화면별 톤 통일
- XML Layout 정렬 및 간격 조정
- 색상 리소스 분리로 툴바/버튼/텍스트 스타일 일관화

---

### 3.7 코드 병합 및 팀 안정화 기여
- 기능별 브랜치 코드 병합 및 구조 정리
- 공통 컴포넌트 및 스타일 충돌 해결
- 발표용 PPT 및 발표 대본 일부 작성
- 팀원 대상 기능 흐름 설명 및 데모 지원

---

## 4. 사용 기술 스택

### Platform / Language
- Android (Kotlin)

### Architecture & Navigation
- Fragment / Activity
- Jetpack Navigation Component
- NavHostFragment
- NavController
- AppBarConfiguration

### UI
- XML Layout (LinearLayout, ConstraintLayout)
- AppBar / Toolbar
- DrawerLayout
- BottomNavigationView
- ListView / RecyclerView
- AlertDialog

### Data
- Firebase Firestore
- 실시간 리스너
- CRUD 연산

### 기타
- SharedPreferences

---

## 5. 한 줄 요약 (이력서용)
> Jetpack Navigation 기반으로 Android 앱 네비게이션 구조를 통합하고,
> Trip 중심 Fragment 흐름 설계와 Dialog UX, Firestore 실시간 동기화를 구현하여
> 여행 관리 앱의 사용성과 구조 안정성을 크게 개선함.
