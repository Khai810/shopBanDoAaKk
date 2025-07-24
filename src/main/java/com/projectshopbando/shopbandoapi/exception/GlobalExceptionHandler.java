package com.projectshopbando.shopbandoapi.exception;

import com.projectshopbando.shopbandoapi.dtos.response.ResponseObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = RuntimeException.class)
    public ResponseEntity<ResponseObject<?>> handleRuntimeException(RuntimeException ex) {
        ResponseObject<?> res = ResponseObject.builder()
                .status("error")
                .message(ex.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(res);
    }
}
