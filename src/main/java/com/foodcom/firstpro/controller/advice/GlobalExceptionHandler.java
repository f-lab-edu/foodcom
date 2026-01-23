package com.foodcom.firstpro.controller.advice;

import com.foodcom.firstpro.auth.exception.LoginFailureException;
import com.foodcom.firstpro.auth.exception.ResourceNotFoundException;
import com.foodcom.firstpro.auth.exception.TokenException;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor; // ✨ import 추가
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import org.springframework.security.access.AccessDeniedException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final io.micrometer.tracing.SpanCustomizer spanCustomizer; // ✨ Trace Customizer 주입

    // 1. 유효성 검사 실패 (HTTP 400 Bad Request)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        tagError("Validation Failed", ex); // ✨ Trace에 태그 추가

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
            LoginFailureException.class,
            TokenException.class
    })
    public ResponseEntity<ErrorResponse> handleAuthenticationExceptions(Exception ex) {
        tagError("Authentication Error", ex); // ✨ Trace에 태그 추가

        String code;
        if (ex instanceof LoginFailureException) {
            code = "로그인 실패";
        } else if (ex instanceof TokenException) {
            code = "jwt 토큰 오류";
        } else {
            code = "인증 필요/실패";
        }

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED) // HTTP 401
                .body(new ErrorResponse(code, ex.getMessage()));
    }

    // 3. 비즈니스 로직 예외 (HTTP 400 Bad Request)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentEx(IllegalArgumentException ex) {
        tagError("Bad Request", ex);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("잘못된 요청", ex.getMessage()));
    }

    // 4. 리소스 충돌 예외 (HTTP 409 Conflict)
    @ExceptionHandler({
            IllegalStateException.class,
            DataIntegrityViolationException.class
    })
    public ResponseEntity<ErrorResponse> handleConflictExceptions(Exception ex) {
        tagError("Conflict", ex);

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
        log.error(">> 예상치 못한 서버 오류: {}", ex.getMessage(), ex);
        tagError("Internal Server Error", ex); // ✨ 에러 태그

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Internal Server Error", "서버 처리 중 예상치 못한 오류가 발생했습니다."));
    }

    // 찾는 리소스 없을 경우 404
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFoundException(ResourceNotFoundException ex) {
        tagError("Not Found", ex);

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("Resource Not Found", ex.getMessage()));
    }

    // 403
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex) {
        tagError("Access Denied", ex);

        ErrorResponse response = new ErrorResponse("권한 없음", "작성자만 수정/삭제할 수 있습니다.");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    // 파일 입출력 예외 (HTTP 500)
    @ExceptionHandler(java.io.IOException.class)
    public ResponseEntity<ErrorResponse> handleIOException(java.io.IOException ex) {
        log.error(">> 파일 업로드 처리 중 오류 발생: {}", ex.getMessage(), ex);
        tagError("File I/O Error", ex);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("File Upload Error", "파일 처리 중 오류가 발생했습니다."));
    }

    // ✨ Trace 태그 헬퍼 메서드
    private void tagError(String description, Exception ex) {
        try {
            spanCustomizer.tag("error", description); // 빨간불 트리거
            spanCustomizer.tag("exception.message", ex.getMessage() != null ? ex.getMessage() : "null");
            spanCustomizer.tag("exception.type", ex.getClass().getSimpleName());
        } catch (Exception e) {
            // 태깅 중 에러 무시
        }
    }

    // --- ErrorResponse DTO ---
    @Data
    @AllArgsConstructor
    public static class ErrorResponse {
        private String code;
        private Object message;
    }
}