package com.projectshopbando.shopbandoapi.exception;

import com.projectshopbando.shopbandoapi.dtos.response.ResponseObject;
import org.apache.coyote.BadRequestException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;

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

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ResponseObject<?>> handleNotFoundException(NotFoundException ex) {
        ResponseObject<?> res = ResponseObject.builder()
                .status("error")
                .message(ex.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(res);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ResponseObject<?>> handleBadRequestException(BadRequestException ex) {
        ResponseObject<?> res = ResponseObject.builder()
                .status("error")
                .message(ex.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(res);
    }



    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseObject<?>> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        List<String> errorMessages = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .toList();

        ResponseObject<?> res = ResponseObject.builder()
                .status("error")
                .message(errorMessages.toString())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(res);
    }
}
