package com.pancakeswap.nft.publish.controller;

import com.pancakeswap.nft.publish.exception.ListingException;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 全局异常处理器
 * 统一处理API请求中的异常情况
 */
@Slf4j
@RestControllerAdvice
public class ControllerAdvisor extends ResponseEntityExceptionHandler {

    /**
     * 处理参数校验失败异常
     * @param e MethodArgumentNotValidException异常
     * @return 包含错误详情的响应
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return new ResponseEntity<>(new ErrorResponse(LocalDateTime.now(), errors), HttpStatus.BAD_REQUEST);
    }

    /**
     * 处理业务逻辑异常
     * @param e BusinessException异常
     * @return 包含错误信息的响应
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        return new ResponseEntity<>(new ErrorResponse(LocalDateTime.now(), Map.of("message", e.getMessage())), HttpStatus.BAD_REQUEST);
    }

    /**
     * 处理资源未找到异常
     * @param e ResourceNotFoundException异常
     * @return 包含错误信息的响应
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException e) {
        return new ResponseEntity<>(new ErrorResponse(LocalDateTime.now(), Map.of("message", e.getMessage())), HttpStatus.NOT_FOUND);
    }

    /**
     * 处理所有未明确处理的异常
     * @param e 未知异常
     * @return 统一的错误响应
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        return new ResponseEntity<>(new ErrorResponse(LocalDateTime.now(), Map.of("message", e.getMessage())), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
