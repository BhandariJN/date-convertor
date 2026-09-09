package com.dateconvertor_java;

import com.dateconvertor_java.dto.DateConvertResponse;
import com.dateconvertor_java.service.DateConvertorService;
import com.nepalidate.ADBS.NepaliDateConverter.DateRangeNotSupported;
import com.nepalidate.ADBS.NepaliDateConverter.InvalidBsDayOfMonthException;
import com.nepalidate.ADBS.NepaliDateConverter.InvalidDateFormatException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DateConvertorServiceTest {

    private final DateConvertorService service = new DateConvertorService();

    @Test
    void adToBs_convertsKnownDates() {
        assertEquals("2081/01/01", service.convertAdToBs("2024-04-13").bsDate()); // Nepali New Year 2081
        assertEquals("1970/01/01", service.convertAdToBs("1913-04-13").bsDate()); // earliest supported
        assertEquals("2083/02/08", service.convertAdToBs("2026-05-22").bsDate());
    }

    @Test
    void bsToAd_convertsKnownDates() {
        assertEquals("2024-04-13", service.convertBsToAd("2081/01/01").adDate());
        assertEquals("1913-04-13", service.convertBsToAd("1970/01/01").adDate());
        assertEquals("2026-05-22", service.convertBsToAd("2083/02/08").adDate());
        assertEquals("2027-01-02", service.convertBsToAd("2083/09/18").adDate());
    }

    @ParameterizedTest
    @CsvSource({
            "2000,1,30,1943-04-14", "2000,10,30,1944-01-14",
            "2062,1,31,2005-04-14", "2062,2,31,2005-05-15",
            "2083,6,31,2026-09-17", "2085,5,30,2028-08-17",
            "2086,1,30,2029-04-14", "2087,9,30,2030-12-17",
            "2089,1,30,2032-04-15", "2090,1,30,2033-04-15",
            "2092,1,30,2035-04-16", "2093,1,30,2036-04-15",
            "2097,4,31,2040-07-17", "2098,11,29,2042-02-13",
            "2100,5,31,2043-08-18", "2100,6,30,2043-09-18",
            "2100,8,30,2043-11-17", "2100,9,29,2043-12-17",
            "2100,12,31,2044-03-14"
    })
    void correctedHamroPatroBoundaries(int year, int month, int days, String firstAdDate) {
        assertEquals(days, service.daysInBsMonth(year, month));
        assertEquals(firstAdDate, service.convertBsToAd(String.format("%04d/%02d/01", year, month)).adDate());
    }

    @ParameterizedTest
    @CsvSource({
            "2088,2031-04-16", "2089,2032-04-15", "2090,2033-04-15", "2091,2034-04-15",
            "2092,2035-04-16", "2093,2036-04-15", "2094,2037-04-15", "2095,2038-04-15",
            "2096,2039-04-16", "2097,2040-04-14"
    })
    void correctedNewYearBoundaries(int year, String firstAdDate) {
        assertEquals(firstAdDate, service.convertBsToAd(year + "/01/01").adDate());
    }

    @Test
    void response_echoesBothDates() {
        DateConvertResponse response = service.convertAdToBs("2026-08-05");
        assertEquals("2026-08-05", response.adDate());
        assertEquals("2083/04/20", response.bsDate());
    }

    @Test
    void roundTrip_preservesTheDate() {
        DateConvertResponse toBs = service.convertAdToBs("2026-08-05");
        DateConvertResponse back = service.convertBsToAd(toBs.bsDate());
        assertEquals("2026-08-05", back.adDate());
    }

    @Test
    void invalidAdFormat_throws() {
        assertThrows(InvalidDateFormatException.class, () -> service.convertAdToBs("05-08-2026"));
        assertThrows(InvalidDateFormatException.class, () -> service.convertAdToBs("2026-13-40"));
    }

    @Test
    void invalidBsMonthOrYear_throwsRange() {
        assertThrows(DateRangeNotSupported.class, () -> service.convertBsToAd("2083/13/01"));
        assertThrows(DateRangeNotSupported.class, () -> service.convertBsToAd("2101/01/01"));
    }

    @Test
    void invalidBsDay_throwsDayException() {
        assertThrows(InvalidBsDayOfMonthException.class, () -> service.convertBsToAd("2083/01/35"));
    }

    @Test
    void blankInput_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> service.convertAdToBs("   "));
        assertThrows(IllegalArgumentException.class, () -> service.convertBsToAd(null));
    }
}
