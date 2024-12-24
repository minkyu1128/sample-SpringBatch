# Spring Batch Sample Project

이 프로젝트는 Spring Batch를 활용한 배치 처리 샘플 애플리케이션입니다.

## 프로젝트 구조

src/main/java/com/minkyu/samplebatch/

├── api/   
│ ├── controller/   
│ │ ├── BatchController.java   
│ │ └── JobController.java   
│ ├── dto/   
│ │ ├── request/   
│ │ │ └── JobLaunchRequest.java   
│ │ ├── response/   
│ │ │ └── JobExecutionResponse.java   
│ │ └── validation/   
│ │ └── ValidationGroups.java   
│ ├── exception/   
│ │ ├── BatchException.java   
│ │ ├── ErrorResponse.java   
│ │ └── GlobalExceptionHandler.java   
│ └── service/   
│ └── JobManagementService.java   
├── common/   
│ ├── config/   
│ │ ├── BatchConfig.java   
│ │ └── SwaggerConfig.java   
│ └── monitoring/   
│ └── BatchMetrics.java   
├── domain/   
│ ├── Person.java   
│ └── PersonRepository.java   
├── job/   
│ └── sample/   
│ ├── chunk/   
│ │ ├── SampleProcessor.java   
│ │ ├── SampleReader.java   
│ │ └── SampleWriter.java   
│ └── listener/   
│ └── SampleJobListener.java   
└── SampleBatchApplication.java   





## 주요 기능

### 1. 배치 작업 관리
- 배치 작업 등록
- 배치 작업 실행
- 작업 상태 조회
- 실행 이력 조회
- 실행 중인 작업 중지

### 2. 샘플 배치 작업
- Person 엔티티 데이터 처리
- 이메일 대문자 변환 처리
- JPA를 활용한 데이터 읽기/쓰기

## API 엔드포인트

### Job Controller
* POST /api/jobs - 배치 Job 등록
* POST /api/jobs/{jobName}/execute - 배치 작업 실행   
* GET /api/jobs/{jobName}/status/{id} - 작업 상태 조회   
* GET /api/jobs/{jobName}/executions - 실행 이력 조회   
* POST /api/jobs/{jobName}/stop/{id} - 작업 중지  


## 기술 스택
- Java 17
- Spring Boot 2.7.3
- Spring Batch
- Spring Data JPA
- Swagger/OpenAPI
- Micrometer
- Lombok
- H2 Database



## 실행 방법

1. 프로젝트 빌드
```bash
./gradlew clean build
```

2. 애플리케이션 실행
```bash
java -jar build/libs/sample-batch-0.0.1-SNAPSHOT.jar
```

3. API 문서   
http://localhost:8080/swagger-ui.html



## 모니터링

다음 메트릭들이 수집됩니다:
- batch.job.executions: 작업 실행 횟수
- batch.job.duration: 작업 실행 시간
- batch.step.read.count: 읽기 항목 수
- batch.step.write.count: 쓰기 항목 수
- batch.step.skip.count: 건너뛰기 항목 수