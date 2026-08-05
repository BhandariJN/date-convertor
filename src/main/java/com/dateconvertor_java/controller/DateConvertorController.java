package com.dateconvertor_java.controller;

import com.dateconvertor_java.dto.DateConvertResponse;
import com.dateconvertor_java.dto.DateRequest;
import com.dateconvertor_java.service.DateConvertorService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;
import java.util.Map;

/**
 * Date conversion REST API built on the ADBS library.
 *
 * <pre>
 * POST /api/convert/ad-to-bs   {"date": "2026-08-05"}   → {"adDate":"2026-08-05","bsDate":"2083/04/20"}
 * POST /api/convert/bs-to-ad   {"date": "2083/04/20"}   → {"bsDate":"2083/04/20","adDate":"2026-08-05"}
 * </pre>
 */
@RestController
@RequestMapping("/api/convert")
public class DateConvertorController {

    private final DateConvertorService convertorService;

    public DateConvertorController(DateConvertorService convertorService) {
        this.convertorService = convertorService;
    }

    /** Converts an English (Gregorian) date {@code yyyy-MM-dd} to a Nepali (Bikram Sambat) date. */
    @PostMapping("/ad-to-bs")
    public DateConvertResponse adToBs(@RequestBody DateRequest request) {
        return convertorService.convertAdToBs(request.date());
    }

    /** Converts a Nepali (Bikram Sambat) date {@code yyyy/MM/dd} to an English (Gregorian) date. */
    @PostMapping("/bs-to-ad")
    public DateConvertResponse bsToAd(@RequestBody DateRequest request) {
        return convertorService.convertBsToAd(request.date());
    }

    /** Number of days in a month. {@code kind} is {@code ad} (Gregorian) or {@code bs} (Bikram Sambat). */
    @GetMapping("/days-in-month")
    public Map<String, Integer> daysInMonth(@RequestParam String kind, @RequestParam int year, @RequestParam int month) {
        if (kind.equals("ad")) {
            return Map.of("days", YearMonth.of(year, month).lengthOfMonth());
        }
        if (kind.equals("bs")) {
            return Map.of("days", convertorService.daysInBsMonth(year, month));
        }
        throw new IllegalArgumentException("kind must be \"ad\" or \"bs\".");
    }
}
