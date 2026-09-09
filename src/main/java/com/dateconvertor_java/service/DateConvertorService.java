package com.dateconvertor_java.service;

import com.dateconvertor_java.dto.DateConvertResponse;
import com.dateconvertor_java.NepaliDateConverter;
import org.springframework.stereotype.Service;

/**
 * Converts dates using the project-owned calendar table verified against Hamro Patro.
 */
@Service
public class DateConvertorService {

    /** Converts a Gregorian date ({@code yyyy-MM-dd}) to a Bikram Sambat date ({@code yyyy/MM/dd}). */
    public DateConvertResponse convertAdToBs(String adDate) {
        if (adDate == null || adDate.isBlank()) {
            throw new IllegalArgumentException("English date is required in YYYY-MM-DD format (example: 2026-08-05).");
        }
        String trimmed = adDate.trim();
        return new DateConvertResponse(trimmed, NepaliDateConverter.adToBs(trimmed));
    }

    /** Converts a Bikram Sambat date ({@code yyyy/MM/dd}) to a Gregorian date ({@code yyyy-MM-dd}). */
    public DateConvertResponse convertBsToAd(String bsDate) {
        if (bsDate == null || bsDate.isBlank()) {
            throw new IllegalArgumentException("Nepali date is required in YYYY/MM/DD format (example: 2083/04/15).");
        }
        String trimmed = bsDate.trim();
        return new DateConvertResponse(NepaliDateConverter.bsToAd(trimmed), trimmed);
    }

    /** Number of days in a Bikram Sambat month (varies by year; 29–32). Returns 0 for an invalid year/month. */
    public int daysInBsMonth(int year, int month) {
        return NepaliDateConverter.daysInMonth(year, month);
    }
}
