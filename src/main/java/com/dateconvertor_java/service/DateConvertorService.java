package com.dateconvertor_java.service;

import com.dateconvertor_java.dto.DateConvertResponse;
import com.nepalidate.ADBS.NepaliDateConverter.AdBs;
import com.nepalidate.ADBS.NepaliDateConverter.NDC;
import org.springframework.stereotype.Service;

/**
 * Wraps the ADBS {@link NDC} converter.
 *
 * <p>NDC is instantiated directly (it has a public no-arg constructor and no
 * runtime Spring dependencies) rather than component-scanned, so this app does
 * not need to scan the {@code com.nepalidate} package.
 */
@Service
public class DateConvertorService {

    private final NDC ndc = new NDC();

    /** Converts a Gregorian date ({@code yyyy-MM-dd}) to a Bikram Sambat date ({@code yyyy/MM/dd}). */
    public DateConvertResponse convertAdToBs(String adDate) {
        if (adDate == null || adDate.isBlank()) {
            throw new IllegalArgumentException("English date is required in YYYY-MM-DD format (example: 2026-08-05).");
        }
        String trimmed = adDate.trim();
        return new DateConvertResponse(trimmed, ndc.adToBs(trimmed));
    }

    /** Converts a Bikram Sambat date ({@code yyyy/MM/dd}) to a Gregorian date ({@code yyyy-MM-dd}). */
    public DateConvertResponse convertBsToAd(String bsDate) {
        if (bsDate == null || bsDate.isBlank()) {
            throw new IllegalArgumentException("Nepali date is required in YYYY/MM/DD format (example: 2083/04/15).");
        }
        String trimmed = bsDate.trim();
        return new DateConvertResponse(ndc.bsToAd(trimmed), trimmed);
    }

    /** Number of days in a Bikram Sambat month (varies by year; 29–32). Returns 0 for an invalid year/month. */
    public int daysInBsMonth(int year, int month) {
        AdBs adBs = new AdBs();
        for (int day = 32; day >= 1; day--) {
            try {
                if (adBs.validateBsDate(year, month, day)) {
                    return day;
                }
            } catch (RuntimeException e) {
                // invalid day for this month, keep trying lower days
            }
        }
        return 0;
    }
}
