package com.dateconvertor_java.dto;

/** Standard error payload: {@code {"error": "human readable message"}}. */
public record ErrorResponse(String error) {
}
