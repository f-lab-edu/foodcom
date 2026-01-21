# Build stage
FROM node:20-alpine AS builder
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

# Runtime stage
FROM nginx:alpine

# 빌드된 정적 파일들을 Nginx의 기본 정적 파일 경로로 복사
COPY --from=builder /app/dist /usr/share/nginx/html

# 커스텀 Nginx 설정 템플릿 복사 (환경변수 주입을 위해)
COPY nginx.conf.template /etc/nginx/conf.d/default.conf.template

# 엔트리포인트 스크립트 복사
COPY docker-entrypoint.sh /docker-entrypoint.sh
RUN chmod +x /docker-entrypoint.sh

EXPOSE 80

# Cloud Run에서 환경변수를 주입받아 nginx 설정 생성 후 실행
ENTRYPOINT ["/docker-entrypoint.sh"]

