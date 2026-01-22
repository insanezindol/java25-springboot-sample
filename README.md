![header](https://capsule-render.vercel.app/api?type=wave&color=auto&height=300&section=header&text=java25%20springboot&fontSize=90)

# java25-springboot-sample 📊

간단한 Spring Boot 예제 프로젝트입니다. 이 리포지토리는 MySQL, Redis, Elasticsearch, Kafka(Producer), Prometheus(Actuator) 연동 예제를 포함합니다.

---

## 기술 스택

- Java 25
- Spring Boot 4.0.1
  - spring-boot-starter-webmvc
  - spring-boot-starter-data-jpa (MySQL)
  - spring-boot-starter-data-redis (Redis, Lettuce)
  - spring-boot-starter-data-elasticsearch (Elasticsearch)
  - spring-boot-starter-kafka (Kafka Producer)
  - spring-boot-starter-actuator (Actuator + Prometheus)
- Micrometer Prometheus registry
- SpringDoc OpenAPI (Swagger UI)
- Lombok
- MySQL Connector/J

(자세한 의존성은 `build.gradle` 참조)

---

## 주요 기능

- MySQL 기반 간단한 User CRUD API
- Redis를 사용한 캐시, 리스트(최근 항목), 세트(장바구니), 카운터 예제
- Elasticsearch 문서 저장/조회/검색 예제
- Kafka Producer를 통한 JSON 메시지 발행 예제
- Spring Actuator + Micrometer를 통한 Prometheus 메트릭 노출
- OpenAPI(Swagger)로 자동 문서화

---

## 프로젝트 구조 (주요 경로)

- `src/main/java/com/example/sample`
  - `controller` - REST 컨트롤러 (UserController, RedisController, ElasticsearchController, KafkaController)
  - `service` - 비즈니스 로직 (RedisService, ElasticsearchService, KafkaProducerService 등)
  - `repository` - Redis/Elasticsearch 레포지토리 및 JPA 레포지토리
  - `domain` - JPA 엔티티 및 도메인 객체 (예: `User`, `ProductDoc`)
- `src/main/resources/application.yml` - 애플리케이션 설정
- `infra/docker-compose.yml` - 개발용 Docker Compose (MySQL, Redis, Kafka, Elasticsearch, Prometheus 등)
- `build.gradle` - 빌드/의존성 정의

---

## 빠른 시작 - 개발 환경

1) 의존 서비스(데이터베이스, 메시지 브로커 등)를 Docker Compose로 띄우기 (프로젝트 루트에서 실행)

```shell
# infra 폴더의 docker-compose.yml로 MySQL/Redis/Elasticsearch/Kafka/Prometheus 등을 띄웁니다
docker-compose -f infra\docker-compose.yml up -d
```

2) 애플리케이션 실행 (개발 모드)

```shell
# gradlew.bat을 사용하여 애플리케이션 실행
.\gradlew.bat bootRun

# 또는 빌드 후 JAR 실행
.\gradlew.bat build ; java -jar build\libs\java25-springboot-sample-0.0.1-SNAPSHOT.jar
```

3) 테스트 실행 및 정리

```shell
# Docker Compose 정리
docker-compose -f infra\docker-compose.yml down
```

포트 요약 (기본)
- 애플리케이션: 8080
- MySQL: 3306
- Redis: 6379
- Elasticsearch: 9200
- Kafka: 9092
- Prometheus: 9090
- Grafana: 3000

---

## 주요 API 엔드포인트

기본 컨텍스트: `/api/v1`

1) MySQL (User CRUD)
- POST /api/v1/mysql
  - Request: UserRequestDto JSON (예: { "name": "홍길동", "email": "hong@example.com" })
  - Response: 생성된 user id (Long)
- GET /api/v1/mysql
  - Response: 모든 User 리스트
- GET /api/v1/mysql/{id}
  - Response: 단건 조회
- PUT /api/v1/mysql/{id}
- DELETE /api/v1/mysql/{id}

2) Redis
- POST /api/v1/redis/users
  - Body: RedisUserDto JSON
- GET /api/v1/redis/users/{userId}
- DELETE /api/v1/redis/users/{userId}
- POST /api/v1/redis/users/{userId}/recent?itemId={itemId}
- GET /api/v1/redis/users/{userId}/recent
- POST /api/v1/redis/users/{userId}/cart (Body: ["item1","item2"])
- GET /api/v1/redis/users/{userId}/cart
- POST /api/v1/redis/items/{itemId}/view (조회수 증가)
- GET /api/v1/redis/items/{itemId}/views
- GET /api/v1/redis/keys?pattern=*

3) Elasticsearch
- POST /api/v1/elasticsearch  (Body: ProductDoc)
- GET /api/v1/elasticsearch/{id}
- GET /api/v1/elasticsearch/search?name={keyword}
- PUT /api/v1/elasticsearch/{id}
- DELETE /api/v1/elasticsearch/{id}

4) Kafka (Producer)
- POST /api/v1/kafka/publish?userId={userId}&action={action}
  - 서버는 `KafkaProducerService`를 사용해 JSON 메시지를 topic에 전송

예시:

```powershell
curl -X POST "http://localhost:8080/api/v1/kafka/publish?userId=123&action=login"
```

5) 문서화 및 헬스
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs
- Actuator health: http://localhost:8080/actuator/health
- Prometheus metrics: http://localhost:8080/actuator/prometheus

---

## 설정 및 환경 변수

프로젝트의 `src/main/resources/application.yml`에 다음과 같은 주요 설정이 있습니다 (요약):

- spring.datasource: MySQL 접속 정보

- spring.jpa.hibernate.ddl-auto: update  (개발용 - 운영 시 변경 필요)

- spring.data.redis: Redis 접속 (host, port, password, database, client-type: lettuce)

- spring.elasticsearch.uris: Elasticsearch URI (기본: http://localhost:9200)

- spring.kafka.bootstrap-servers: Kafka 브로커 주소
  - consumer/producer 기본 직렬화/역직렬화 설정은 `application.yml` 참조

- management.endpoints.web.exposure.include: health,info,prometheus
  - Actuator가 Prometheus 메트릭을 `/actuator/prometheus`로 노출

주요 환경 변수 (요약):
- MYSQL_HOST, MYSQL_PORT, MYSQL_DATABASE, MYSQL_USER, MYSQL_PASSWORD
- REDIS_HOST, REDIS_PORT, REDIS_PASSWORD
- ELASTICSEARCH_URIS
- KAFKA_BOOTSTRAP_SERVERS

---

## Docker Compose (infra/docker-compose.yml)

`infra/docker-compose.yml`에는 개발용으로 MySQL, Redis, Kafka(KRaft), Elasticsearch, Prometheus, Grafana 등이 정의되어 있습니다. 파일의 주요 포인트:

- MySQL: `mysql:8.0`, 초기 DB/사용자/비밀번호는 compose 파일의 environment 값 참조 (예: `devdb` / `devuser` / `devpassword`)
- Redis: `redis:7-alpine`, `requirepass redispassword`로 비밀번호 설정
- Kafka: KRaft 모드로 간단 구성 (포트 9092)
- Elasticsearch: single-node 모드
- Prometheus: `infra/prometheus/prometheus.yml`을 마운트하여 설정
- Grafana: 선택적으로 포함되어 있음

Kafka 토픽 기본값: `kafka.topic.event: user-events` (application.yml 참조). docker-compose의 Kafka는 `KAFKA_AUTO_CREATE_TOPICS_ENABLE: "true"`로 자동 생성 허용.

---

## 모니터링

- Actuator 엔드포인트로 헬스/메트릭을 제공
  - `GET /actuator/health`
  - `GET /actuator/prometheus` (Prometheus가 스크랩)
- Prometheus는 `infra/prometheus/prometheus.yml`을 통해 애플리케이션 메트릭을 스크랩하도록 구성
- Grafana 프로비저닝 파일을 사용해 대시보드 자동 설정 가능
