#!/bin/sh

# 환경변수를 nginx 설정에 주입
envsubst '${BACKEND_URL}' < /etc/nginx/conf.d/default.conf.template > /etc/nginx/conf.d/default.conf

# 생성된 설정 파일 확인 (디버깅용)
cat /etc/nginx/conf.d/default.conf

# nginx 실행
exec nginx -g 'daemon off;'
