# 📈 IOTL Backend

주식 모의투자 시스템의 백엔드 레포지토리입니다.  
Spring Boot 기반으로, 매수/매도 트랜잭션, 계좌 관리, 환전 기능 등을 제공합니다.

---

## 🛠️ 기술 스택

- Java 17
- Spring Boot 3.x
- Spring Data JPA
- MySQL
- Gradle
- Lombok
- SpringDotenv (환경변수 관리)
- IntelliJ / VS Code

---

## 📁 프로젝트 구조

```bash
📦 IOTL
├── src/main/java/com/example/iotl
│   ├── controller     # API 컨트롤러
│   ├── domain         # Entity 클래스
│   ├── repository     # JPA 리포지토리
│   ├── service        # 비즈니스 로직
│   └── IotlApplication.java
├── src/main/resources
│   ├── application.yml
├── .env               # 환경 변수 파일 (gitignore 대상)
├── .gitignore
├── README.md
└── build.gradle
```

---

## ⚙️ 개발 환경 세팅

### 1. Java 17, Gradle 설치

- Java 17 이상 설치
- Gradle은 Wrapper로 자동 설정됨 (`./gradlew` 사용)

### 2. 환경변수(.env)를 사용하는 이유

#### 1. 설정 관리가 쉬워진다

설정을 코드가 아닌 외부 파일에서 관리함으로써, 코드 수정 없이도 환경 설정을 변경할 수 있다.  
예를 들어, DB 주소나 포트 번호를 수정할 때 `.env` 파일만 수정하면 된다.

#### 2. 보안에 유리하다

DB 비밀번호, API 키처럼 민감한 정보를 코드에 직접 작성할 경우, Git에 실수로 업로드될 위험이 있다.  
이를 `.env` 파일로 분리해 관리함으로써 민감 정보가 노출되는 상황을 방지할 수 있다.

#### 3. 다양한 환경에 유연하게 대응할 수 있다

개발, 테스트, 운영 등 다양한 환경에서 애플리케이션을 실행할 때, 환경별 `.env` 파일을 통해 맞춤형 설정을 적용할 수 있다.  
예를 들어, `.env.dev`, `.env.prod` 등을 나눠 관리하면 하나의 코드베이스로 여러 환경을 지원할 수 있다.



---

## 🚀 실행 방법

```bash

```

또는 IDE에서 `IotlApplication.java` 실행

---

## 🧪 API 테스트

> Postman 또는 브라우저에서 테스트 가능

```http
POST http://localhost:8080/test?name=hello
```

---

## 🗂️ Git 브랜치 전략

- `main`: 운영용 브랜치
- `dev`: 개발 브랜치 (기본 브랜치)
- 기능별 브랜치: `feature/거래-기능`, `feature/계좌-생성`, ...

---

## 📝 커밋 컨벤션

- 커밋 메시지는 항상 제목(Title)을 포함하며 `[TYPE] 커밋 내용 #이슈번호` 형식을 따릅니다.
- `subject`는 현재형, 명령문 형태로 작성합니다.
- 본문(Body), 꼬리말(Footer)은 필요한 경우에만 작성합니다.

### ✅ Commit Type

| 타입      | 설명                                       |
|-----------|--------------------------------------------|
| FEAT      | 새로운 기능 추가                           |
| FIX       | 버그 수정                                  |
| DOCS      | 문서 수정 (README, 주석 등)                |
| STYLE     | 코드 포맷팅, 세미콜론 누락 등 구조 미변경  |
| REFACTOR  | 코드 리팩토링 (기능 변경 없이 구조 개선)   |
| TEST      | 테스트 코드 추가 및 수정                   |
| CHORE     | 빌드 업무 수정, 패키지 매니저 설정 변경    |
| HOTFIX    | 긴급한 버그 수정                           |
| DELETE    | 파일/코드 삭제                             |
| TEMP      | 임시 커밋 (나중에 정리 예정)               |
| MERGE     | 브랜치 병합                                |

### ✍ 작성 예시

```bash
[FEAT] 사용자 로그인 기능 추가 #12
[FIX] 비밀번호 암호화 오류 수정 #20
[REFACTOR] 거래 로직 구조 개선 #33
```

---

## 🙋 백엔드 팀원 역할 (추후 작성 완료)

| 이름  | 역할                                   |
|-----|--------------------------------------|
| 양서진 | 백엔드 팀장 (환경설정, 트랜잭션 설계, 매도, 매매,DB 설계) |
| 이호진 |                                      |
| 이혜원 |                                      |
| 김보성 |                                      |



---


