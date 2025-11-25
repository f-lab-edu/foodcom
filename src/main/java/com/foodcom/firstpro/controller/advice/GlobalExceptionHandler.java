package com.foodcom.firstpro.controller.advice;

import com.foodcom.firstpro.auth.exception.LoginFailureException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j; // ✨ 로그 추가
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j // ✨ 로그 추가
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. 유효성 검사 실패 (HTTP 400 Bad Request)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex){
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            String fieldName = error.getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("Validation Failed", errors));
    }

    // 2. 인증/권한 관련 예외 (HTTP 401 Unauthorized)
    @ExceptionHandler({
            SecurityException.class,
            LoginFailureException.class
    })
    public ResponseEntity<ErrorResponse> handleAuthenticationExceptions(Exception ex) {
        String code = (ex instanceof LoginFailureException) ? "로그인 실패" : "인증 필요/실패";

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse(code, ex.getMessage()));
    }

    // 3. 비즈니스 로직 예외 (HTTP 400 Bad Request)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentEx(IllegalArgumentException ex) {
        // 클라이언트 요청의 잘못된 데이터(예: 존재하지 않는 사용자 ID로 조회)에 대해 400 반환
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("잘못된 요청", ex.getMessage()));
    }

    // 4. 리소스 충돌 예외 (HTTP 409 Conflict)
    @ExceptionHandler({
            IllegalStateException.class, // 비즈니스 로직 중복 (예: 이미 가입된 아이디)
            DataIntegrityViolationException.class // DB 제약 조건 위반 (예: UNIQUE 키 중복)
    })
    public ResponseEntity<ErrorResponse> handleConflictExceptions(Exception ex) {
        // DB 무결성 오류의 경우, 상세 메시지를 숨기고 일반적인 오류 메시지를 제공할 수 있습니다.
        String message = (ex instanceof DataIntegrityViolationException)
                ? "데이터 무결성 충돌이 발생했습니다. (예: 중복된 아이디)"
                : ex.getMessage();

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("리소스 충돌", message));
    }


    // 5. 서버 내부 런타임 예외 (HTTP 500 Internal Server Error)
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeError(RuntimeException ex) {
        log.error(">> 예상치 못한 서버 오류: {}", ex.getMessage(), ex); // 상세 로그 기록

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Internal Server Error", "서버 처리 중 예상치 못한 오류가 발생했습니다."));
    }

    // --- ErrorResponse DTO ---
    @Data
    @AllArgsConstructor
    public static class ErrorResponse {
        private String code;
        private Object message;
    }
}