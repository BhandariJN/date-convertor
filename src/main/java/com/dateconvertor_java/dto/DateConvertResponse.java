package com.dateconvertor_java.dto;

/**
 * Conversion result. Exactly one of {@code adDate} / {@code bsDate} is the input
 * and the other is the converted output, so the payload is identical for both
 * directions.
 */
public record DateConvertResponse(String adDate, String bsDate) {
}
