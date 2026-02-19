# 🍔 FoodCom - Scalable Food Community Service

> **🔴 프로젝트 상태:** 부하 테스트(1000 VUs) 및 성능 검증 완료 후 비용 절감을 위해 GCP 인프라(Cloud SQL, Redis, Storage, Cloud Run)를 종료했습니다. 코드와 아키텍처 설계는 그대로 유지되어 있으며, 언제든 재배포 가능합니다.

**FoodCom**은 대규모 트래픽을 처리할 수 있도록 설계된 음식 커뮤니티 SNS 서비스입니다.
구글 클라우드(GCP)의 핵심 기술을 활용하여 **서버리스 확장성(Serverless Scalability)**과 **데이터 일관성(Data Consistency)**을 동시에 확보했습니다.

---

## 🏗 System Architecture (핵심)

이 프로젝트의 가장 큰 특징은 **"실제 운영 환경(Production)을 고려한 엔지니어링"**입니다.
단순히 기능만 구현하는 것을 넘어, 대용량 트래픽과 안정성을 위한 아키텍처를 구축했습니다.


```mermaid
graph TD
    User(User) --> GLB["Global Load Balancer & Cloud CDN"]
    GLB -->|"Static Assets (Cache)"| Bucket["Cloud Storage (Media)"]
    GLB -->|"API Requests"| Run["Cloud Run (Backend Autoscale)"]
    
    subgraph "Backend Infrastructure"
        Run -->|"Write (Transactional)"| Master[("Cloud SQL Master - Write")]
        Run -->|"Read (Transactional readOnly)"| Slave[("Cloud SQL Replica - Read")]
        Run -->|"Auth Token Storage"| Redis[("Memorystore Redis")]
    end
    
    subgraph "CI/CD Pipeline"
        Github[GitHub Actions] -->|"Build & Test"| Artifact[Artifact Registry]
        Artifact -->|Deploy| Run
    end
```

## 🛠 Technology Stack

### Backend
*   **Framework:** Spring Boot 3.4
*   **Language:** Java 21 LTS
*   **ORM:** Spring Data JPA
*   **Security:** Spring Security + JWT + OAuth2
*   **Build Tool:** Gradle

### Infrastructure (GCP)
*   **Compute:** Cloud Run (Serverless Container)
*   **Database:** Cloud SQL (MySQL 8.0) - HA(High Availability) 구성 가능
*   **Auth Store:** Memorystore for Redis
*   **Storage:** Cloud Storage + Cloud CDN

### Frontend
*   **Framework:** React + TypeScript + Vite
*   **State Management:** Zustand
*   **Styling:** Tailwind CSS

---



### 💡 Key Technical Highlights

#### 1. Database Read/Write Splitting (부하 분산)
대부분의 서비스는 **읽기(Read)가 쓰기(Write)보다 8:2 비율로 많다**는 점에 착안했습니다.
*   **RoutingDataSource 구현:** `@Transactional(readOnly = true)` 어노테이션 유무에 따라 트래픽을 자동으로 분리합니다.
*   **Master (Write):** 데이터 변경 작업 전담. 안전한 구글 **Socket Factory** 터널링 사용.
*   **Replica (Read):** 단순 조회 작업 전담. 부하가 몰려도 Master 성능에 영향을 주지 않음.

#### 2. Cloud Native Scalability (무중단 확장)
*   **Cloud Run (Compute):** 트래픽이 0일 때는 0원으로, 트래픽이 폭주하면 **인스턴스가 수천 개까지 1초 만에 확장**됩니다.
*   **Cloud SQL (Database):** Read Replica를 통해 읽기 성능을 수평적으로 확장(Scale-out)할 수 있는 구조입니다.

#### 3. Performance Optimization (속도 최적화)
*   **Global CDN (Edge Caching):** 사용자가 업로드한 이미지 파일은 **Cloud CDN**을 통해 전 세계 엣지 로케이션에 캐싱됩니다. (응답 속도 < 20ms)
*   **Security & Auth (Redis):** JWT 인증 시 필요한 **Refresh Token**을 고성능 In-Memory DB인 Redis에 저장하여, 빠르고 안전한 인증 처리를 구현했습니다. (DB 부하 없이 토큰 검증)

#### 4. Robust CI/CD & Security
*   **GitHub Actions:** 코드 푸시 시 `Test -> Build -> Docker Push -> Traffic Splitting Deploy` 전 과정이 자동화되어 있습니다.
*   **Zero-Downtime Deployment:** 배포 중에도 서비스가 중단되지 않도록 **트래픽을 서서히 이동시키는(Traffic Splitting)** 전략을 사용했습니다.

---

## �️ 트러블슈팅 및 기술적 도전 (Troubleshooting & Challenges)

프로젝트를 진행하며 마주쳤던 난관들과 이를 기술적으로 해결한 과정입니다.

### 1. Cloud Run 메모리 부족 (OOM)과 컨테이너 셧다운
*   **문제 발생:** 배포된 애플리케이션이 시작되다가 `137` 종료 코드(OOM Killer)와 함께 비정상 종료되거나, 이미지 업로드 처리 중 간헐적으로 500 에러 발생.
*   **원인 분석:** Cloud Run의 기본 메모리 할당량은 512MB인데, JVM 기반의 Spring Boot 애플리케이션이 구동되고 힙 메모리를 사용하기에는 턱없이 부족했음.
*   **해결:**
    *   `deploy.yml`의 배포 옵션에 `--memory 1Gi` (또는 2Gi) 옵션을 추가하여 컨테이너 메모리 할당량을 증설.
    *   JVM 옵션으로 `-XX:MaxRAMPercentage=75.0`을 설정하여, 컨테이너 메모리 내에서 효율적으로 힙을 사용하도록 튜닝.

### 2. CORS(Cross-Origin) 정책 위반과 프론트엔드 통신 실패
*   **문제 발생:** 로컬(`localhost:3000`)에서는 잘 되던 API 호출이, 배포 후 프론트엔드(`foodcom-frontend...`)에서 백엔드를 호출할 때 브라우저 콘솔에 빨간색 CORS 에러를 뿜으며 차단됨.
*   **원인 분석:** 브라우저의 보안 정책상 도메인이 다른 곳으로의 요청은 서버가 명시적으로 허용(`Access-Control-Allow-Origin`)해 주어야 함.
*   **해결:**
    *   Spring Security 설정(`SecurityConfig`)에 `CorsConfigurationSource` 빈을 등록.
    *   프론트엔드 배포 URL과 로컬 개발 URL을 명시적으로 허용 목록(`setAllowedOrigins`)에 추가하고, `Allow-Credentials: true`를 설정하여 쿠키/헤더 전송을 가능케 함.

### 3. Google Cloud Storage (GCS) 인증 권한 문제 (403 Forbidden)
*   **문제 발생:** 로컬에서는 `GOOGLE_APPLICATION_CREDENTIALS` 환경변수로 json 키 파일을 등록해 잘 동작했으나, Cloud Run 배포 후 이미지 업로드 시 `403 Forbidden` 에러 발생.
*   **원인 분석:**
    *   로컬: 서비스 계정 키 파일(JSON)을 직접 사용.
    *   Cloud Run: 키 파일 없이 **Workload Identity (ADC)**를 사용하는 것이 권장됨.
    *   하지만 Cloud Run 서비스 계정에 "Storage Object Admin" 권한이 누락되어 있었음.
*   **해결:**
    *   IAM(권한 관리)에서 Cloud Run이 사용하는 서비스 계정(`Compute Engine default service account` 등)에 **Storage Object Admin** 역할을 부여.
    *   코드에서는 별도의 키 파일 로딩 없이 `StorageOptions.getDefaultInstance()`를 사용하여 환경에 따라 유연하게 인증 정보를 가져오도록 리팩토링.

### 4. Cloud SQL 500 에러와 라이브러리 호환성 문제
*   **문제 발생:** 배포 환경에서만 로그인이 실패하고 `Connection Refused` 에러 발생.
*   **원인 분석:** `mysql-socket-factory` 라이브러리 구버전(1.15.0)과 최신 MySQL Driver 간의 충돌.
*   **해결:** 라이브러리를 `1.20.0`으로 업그레이드하고, Slave DB 연결 방식을 Socket Factory로 통일.

### 5. Master/Slave 라우팅과 트랜잭션 타이밍
*   **문제 발생:** 읽기 트랜잭션이 Master DB로 라우팅되는 현상.
*   **원인 분석:** 트랜잭션 시작 시점에 이미 커넥션을 확보해버리는 Spring의 조급한(?) 성격.
*   **해결:** `LazyConnectionDataSourceProxy` 도입으로 실제 쿼리 실행 시점까지 커넥션 획득을 지연시킴.

### 6. CI/CD 환경에서의 테스트 격리 실패
*   **문제 발생:** `.gitignore` 된 `application.properties` 때문에 GitHub Actions 빌드 실패.
*   **해결:** GitHub Secrets에 프로덕션 설정을 저장하고, `deploy.yml` 워크플로우 실행 시점에 동적으로 설정 파일을 생성하여 주입하는 방식으로 보안과 편의성을 모두 잡음.

---

## 성능과 안정성 사이 고민하기
* 최소 인스턴스 수와 최대 인스턴스 수 고민하기

  https://velog.io/@jhkang0516/%ED%8A%B8%EB%9E%98%ED%94%BD-%EB%8D%B0%EC%9D%B4%ED%84%B0%EB%A1%9C-%EC%A6%9D%EB%AA%85%ED%95%98%EB%8A%94-%EC%95%84%ED%82%A4%ED%85%8D%EC%B2%98-%EA%B0%9C%EC%84%A0%EA%B8%B0-%EB%82%B4-%EC%84%9C%EB%B2%84%EB%8A%94-%EC%99%9C-27%EC%B4%88-%EB%8F%99%EC%95%88-%EB%A9%88%EC%B7%84%EB%82%98
* CDN 사용을 통한 이미지 빠르게 불러오기

  https://velog.io/@jhkang0516/CDN-%EC%82%AC%EC%9A%A9%EC%9D%84-%EC%9C%84%ED%95%9C-GCP-%EB%A1%9C%EB%93%9C%EB%B0%B8%EB%9F%B0%EC%84%9C-%EC%84%A4%EC%A0%95%ED%95%98%EA%B8%B0

* k6 부하 테스트를 통한 병목 구간 진단 및 성능 향상시키기
  https://velog.io/@jhkang0516/k6-%EB%B6%80%ED%95%98-%ED%85%8C%EC%8A%A4%ED%8A%B8%EB%A5%BC-%ED%86%B5%ED%95%9C-%EB%B3%91%EB%AA%A9-%EA%B5%AC%EA%B0%84Bottleneck-%EC%A7%84%EB%8B%A8-%EB%B0%8F-%ED%95%B4%EA%B2%B0-%EC%A0%84%EB%9E%B5#%EB%82%B4-%EC%BD%94%EB%93%9C%EC%99%80-%ED%8C%8C%EC%9D%BC-%EB%8B%A4%EC%8B%9C-%ED%99%95%EC%9D%B8%ED%95%B4%EB%B3%B4%EA%B8%B0

## 📈 Scalability Verification (부하 테스트)
**k6** 부하 테스트 도구를 사용하여 **Replica Lag**, **Cloud Run Auto-scaling**, 그리고 **DB Connection Pool**의 안정성을 검증했습니다.

### 1. 테스트 환경 & 시나리오
*   **Tools:** k6 (Load Testing), Zipkin (Distributed Tracing)
*   **VUs (Virtual Users):** 50명 (동시 접속)
*   **Scenario:** 
    *   **Read (80%):** 게시글 목록 조회 (Replica DB 활용 확인)
    *   **Write (20%):** 회원가입 (Master DB 부하 확인)

### 2. 성능 최적화 과정 (Tuning Journey)
초기 테스트 시 **높은 레이턴시(Max 30s+)**와 **에러(Timeout)**가 발생하여 단계적으로 최적화를 진행했습니다.

| 단계 | 조치 내용 | 개선 결과 |
| :--- | :--- | :--- |
| **1. Connection Pool & Concurrency** | **Cloud Run Concurrency(80) / HikariCP(20)** 비율 조정 | **Cloud SQL Max Connection(250) 고갈 방지** 및 병목 현상 해소 |
| **2. Query Optimization (N+1)** | **JPA Fetch Join → DTO Projection (QueryDSL/JPQL)** 전환 | 필요한 컬럼만 조회하여 **네트워크 I/O 감소** 및 영속성 컨텍스트 오버헤드 제거 |
| **3. Database Indexing** | 주요 필드(`fk_member_id`, `created_at`) **Covering Index** 적용 | Full Table Scan 방지, 조회 성능 **2배 향상** (Avg 605ms → 360ms) |
| **4. 리소스 충돌 해결**| 테스트 스크립트 ID 생성 로직 개선 (Base36) | 회원가입 시 `409 Conflict` 에러 해결 (에러율 14% → 0%) |

### 🛠️ Detailed Optimization & Reliability Journey

단순히 기능을 구현하는 것을 넘어, **성능(Performance), 확장성(Scalability), 안정성(Stability)** 세 가지 토끼를 잡기 위해 깊이 있게 고민하고 개선한 과정입니다.

#### 1. Database & Schema Optimization (기반 다지기)
*   **UUID vs PK (Long):** 초기에는 `UUID`를 사용했으나, B-Tree 인덱스 정렬 성능 저하와 Page Splitting 문제를 확인하고 **Auto-increment PK(Long)**로 전면 전환하여 Insert 및 조회 성능을 개선했습니다.
*   **Covering Index:** `idx_member_id` (회원별 조회), `idx_modified_at` (최신순 정렬) 등 조회 패턴에 최적화된 인덱스를 적용해 **Full Table Scan을 방지**했습니다.

#### 2. Query Performance (N+1 문제 해결)
*   **Problem:** `Fetch Join`은 연관된 모든 엔티티 데이터를 로딩하여 메모리 낭비가 심했습니다.
*   **Solution:** **Repository에서 DTO Projection** 방식을 도입, 화면에 필요한 데이터만 "콕 집어서" 조회(`SELECT p.id, p.title...`)함으로써 네트워크 I/O와 영속성 컨텍스트 부하를 최소화했습니다.

#### 3. System Scalability (동시성 제어)
Cloud Run(Serverless)의 무한한 확장성과 RDB(Cloud SQL)의 물리적 한계 사이에서 균형을 맞췄습니다.
*   **Math:** `Max Instances * Pool Size ~= DB Max Connections`
*   **Confg:** Cloud Run Concurrency를 **80**으로 높이고, HikariCP Pool은 **20**으로 제한하여, 트래픽 폭주 시에도 DB 커넥션이 고갈되지 않도록 **Backpressure** 역할을 설계했습니다.

#### 4. Infrastructure Security (보안 강화)
*   **Legacy Key 제거:** 보안 사고의 원인이 될 수 있는 JSON 키 파일(`service-account-key.json`)을 프로젝트에서 완전히 제거했습니다.
*   **ADC 도입:** Google Cloud의 **Workload Identity (ADC)**를 도입하여, 로컬에서는 `gcloud auth`로, 배포 환경에서는 IAM 권한만으로 안전하게 Storage에 접근합니다.

#### 5. Full-Stack Reliability (안정성 확보)
*   **Frontend Routing Mismatch:** 프론트엔드(`postUuid`)와 백엔드(`id`) 간의 파라미터 불일치로 인한 500 에러를 디버깅하고, 라우팅 구조를 일관성 있게(`:postId`) 수정했습니다. (Test Coverage 100% 달성)
*   **Upload Stability:** 대용량 이미지 업로드 시 발생하는 500 에러를 해결하기 위해 Spring Boot와 Cloud Run의 힙 메모리 및 업로드 용량 제한을 최적화했습니다.

### 4. 최종 결과 (Final Test Results)
최종 테스트 결과, **에러율 0%** 달성과 함께 평균 응답 속도(Avg Latency)가 **605ms → 360ms**로 2배 가까이 단축되었습니다. 또한 최대 응답 시간(Max Latency)도 30초 이상의 Timeout에서 **1.53초**로 대폭 개선되어 시스템 안정성이 입증되었습니다.

*   **Error Rate:** **0.00%** (Perfect Stability)
*   **Avg Latency:** **~210ms** (쾌적한 응답 속도)
*   **Throughput:** **~21.7 req/s** (분당 약 1,150 요청 처리)

---

## 🚀 High-Load Scaling: 1000 VUs 부하 테스트

50명 → 400명까지의 점진적인 부하 테스트를 거쳐, **1000명 동시 접속(VUs)** 환경에서의 시스템 한계를 테스트하고 최적화를 진행했습니다.

### 1. 테스트 진행 과정 및 병목 분석

| VUs | p(95) Latency | 에러율 | 병목 원인 | 조치 |
| :---: | :---: | :---: | :--- | :--- |
| 50 | ~500ms | 0% | - | 기준선 측정 |
| 200 | ~2.3s (Cold) | 0% | Cold Start | min-instances=1 설정 |
| 400 | ~584ms (Warm) | 0% | Concurrency 한도 | Concurrency 30→50 증가 |
| 1000 | ~5.4s (초기) | 0% | **Slave DB CPU 90%** | **Slave DB vCPU 1→2 업그레이드** |
| 1000 | **~897ms** (최종) | **0%** | - | ✅ 안정화 완료 |

### 2. 리소스 스케일링 판단 근거

#### 🔍 Cloud Run Concurrency vs Max Instances
*   **CPU 사용률 70% 미만 + max-instances 도달** → `max-instances` 또는 `concurrency` 증가 필요
*   **CPU 사용률 90%+ → 설정 조절 무의미**, 하드웨어 스펙 업그레이드 필요

#### 🔍 DB 병목 판단
*   **Cloud Run은 여유, DB CPU 포화(90%)** → Pool Size 조절로 해결 불가
*   **Pool Size는 "버퍼"일 뿐**, 실제 DB 부하를 줄이려면 **캐싱** 또는 **스펙 업그레이드** 필요

#### 🎯 최종 결정
*   **Slave DB:** 1 vCPU → **2 vCPU** (Read 트래픽 80% 처리, CPU 병목 해소)
*   **Master DB:** 현재 스펙 유지 (CPU 30%대로 여유 있음)

### 3. 최종 인프라 설정

| 구성 요소 | 최종 설정 | 비고 |
| :--- | :---: | :--- |
| **Cloud Run Concurrency** | 70 | 인스턴스당 동시 처리 요청 수 |
| **Cloud Run Max Instances** | 15 | 최대 1,050 동시 요청 가능 |
| **Cloud Run Min Instances** | 1 | Cold Start 방지 |
| **Master DB Pool Size** | 60 | Write 트래픽 처리 |
| **Slave DB Pool Size** | 60 | Read 트래픽 처리 |
| **Slave DB vCPU** | **2** | 1000 VUs 대응을 위해 업그레이드 |

### 4. 1000 VUs 최종 테스트 결과

```
█ THRESHOLDS
  http_req_duration ✓ 'p(95)<2000' p(95)=897.17ms
  http_req_failed   ✓ 'rate<0.01' rate=0.00%

█ TOTAL RESULTS
  checks_succeeded: 94.51% (99,657 / 105,436)
  
  ✓ GetPosts duration < 500ms: 87%
  ✓ Signup duration < 1s: 94%
  
  http_req_duration: avg=203ms, med=53ms, max=5.72s, p(95)=897ms
  http_reqs: 52,718 (435 req/s)
```

*   **Error Rate:** **0.00%** ✅
*   **p(95) Latency:** **897ms** (임계값 2s 이내) ✅
*   **Throughput:** **435 req/s** (분당 26,100 요청 처리) ✅
*   **GetPosts 성공률:** 87% < 500ms
*   **Signup 성공률:** 94% < 1s

