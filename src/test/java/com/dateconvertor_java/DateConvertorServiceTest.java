package com.dateconvertor_java;

import com.dateconvertor_java.dto.DateConvertResponse;
import com.dateconvertor_java.service.DateConvertorService;
import com.nepalidate.ADBS.NepaliDateConverter.DateRangeNotSupported;
import com.nepalidate.ADBS.NepaliDateConverter.InvalidBsDayOfMonthException;
import com.nepalidate.ADBS.NepaliDateConverter.InvalidDateFormatException;
import org.junit.jupiter.api.Test;

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
