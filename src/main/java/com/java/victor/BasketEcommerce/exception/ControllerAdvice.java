package com.java.victor.BasketEcommerce.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ControllerAdvice {
    @ExceptionHandler(DataNotFoundExeption.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handlerDataNotFoundException (DataNotFoundExeption ex) {
        return ex.getMessage();
    }

    @ExceptionHandler(BussinesExeption.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handlerBussinesException (BussinesExeption ex) {
        return ex.getMessage();    }
}
