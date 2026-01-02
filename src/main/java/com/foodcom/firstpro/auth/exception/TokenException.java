package com.foodcom.firstpro.auth.exception;

public class TokenException extends RuntimeException {
    public TokenException(String message) {
        super(message);
    }

    // 예외 메시지와 원인 예외를 함께 받는 생성자 (선택 사항)
    public TokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
