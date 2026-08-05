package com.dateconvertor_java.controller;

import com.dateconvertor_java.dto.ErrorResponse;
import com.nepalidate.ADBS.NepaliDateConverter.DateRangeNotSupported;
import com.nepalidate.ADBS.NepaliDateConverter.InvalidBsDayOfMonthException;
import com.nepalidate.ADBS.NepaliDateConverter.InvalidDateFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Maps the ADBS library's validation exceptions (and bad client input) to clean
 * JSON error responses with an HTTP 400 status.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler({
            InvalidDateFormatException.class,
            DateRangeNotSupported.class,
            InvalidBsDayOfMonthException.class,
            IllegalArgumentException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidInput(RuntimeException e) {
        return new ErrorResponse(e.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleUnreadableBody() {
        return new ErrorResponse("Request body must be JSON with a \"date\" field, e.g. {\"date\": \"2026-08-05\"}.");
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleUnexpected(Exception e) {
        log.error("Unexpected error while converting a date", e);
        return new ErrorResponse("Something went wrong on the server. Please try again.");
    }
}
