package com.foodcom.firstpro.auth.exception;

// 로그인 실패를 처리하기 위한 커스텀 RuntimeException
public class LoginFailureException extends RuntimeException {

    public LoginFailureException(String message) {
        super(message);
    }

    public LoginFailureException(String message, Throwable cause) {
        super(message, cause);
    }
}