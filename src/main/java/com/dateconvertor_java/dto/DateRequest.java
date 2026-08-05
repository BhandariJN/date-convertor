package com.dateconvertor_java.dto;

/**
 * Request body for the date conversion endpoints.
 * For AD dates use {@code yyyy-MM-dd}; for BS dates use {@code yyyy/MM/dd}.
 */
public record DateRequest(String date) {
}
