# 1. Java 25 기반 이미지 (Eclipse Temurin 또는 최신 JDK 25 이미지 사용)
FROM eclipse-temurin:25-jdk-jammy

# 2. 작업 디렉토리 설정
WORKDIR /app

# 3. 임시 파일용 볼륨
VOLUME /tmp

# 4. 빌드 시 전달받을 인자 설정
ARG JAR_FILE=build/libs/*.jar
ARG SPRING_ACTIVE_PROFILE=dev

# 5. 환경 변수 설정
ENV USE_PROFILE=${SPRING_ACTIVE_PROFILE}
ENV TZ=Asia/Seoul
# Java 25 및 가상 스레드 최적화 옵션
# -XX:+UseZGC: 지연 시간을 최소화하는 가비지 컬렉터 (대규모 트래픽 처리에 유리)
ENV JAVA_OPTS="-Dfile.encoding=UTF-8 \
               -Djava.security.egd=file:/dev/./urandom \
               -XX:+UseZGC \
               -XX:+ZGenerational"

# 6. 타임존 설정
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 7. JAR 파일 복사 (Gradle 빌드 결과물 경로 기준)
COPY ${JAR_FILE} app.jar

# 8. 실행 명령
ENTRYPOINT exec java ${JAVA_OPTS} -Dspring.profiles.active=${USE_PROFILE} -jar app.jar
