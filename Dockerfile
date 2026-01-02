# Build stage
FROM gradle:8.5-jdk21 AS builder
WORKDIR /app
COPY . .
# .git 디렉토리가 없으면 빌드 정보 생성 실패할 수 있으므로 제외하거나 필요시 복사
# Gradle 데몬 없이 빌드하여 오버헤드 감소
RUN ./gradlew bootJar --no-daemon

# Runtime stage
# 경량화된 Alpine Linux 기반의 JRE 21 이미지 사용
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# 빌드 스테이지에서 생성된 JAR 파일만 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# 컨테이너 실행 시 노출할 포트
EXPOSE 8080

# 애플리케이션 실행
# prod 프로파일 활성화 (선택 사항, 필요 시 환경 변수로 오버라이드 가능)
ENV SPRING_PROFILES_ACTIVE=prod
ENTRYPOINT ["java", "-jar", "app.jar"]
