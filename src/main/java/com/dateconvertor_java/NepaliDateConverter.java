package com.dateconvertor_java;

import com.nepalidate.ADBS.NepaliDateConverter.DateRangeNotSupported;
import com.nepalidate.ADBS.NepaliDateConverter.InvalidBsDayOfMonthException;
import com.nepalidate.ADBS.NepaliDateConverter.InvalidDateFormatException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Date conversion backed by the verified, project-owned calendar table. */
public final class NepaliDateConverter {

    private static final Pattern DATE = Pattern.compile("^(\\d{4})[-/](\\d{1,2})[-/](\\d{1,2})$");
    private static final List<YearData> YEARS = loadYears();

    private NepaliDateConverter() {
    }

    public static String bsToAd(String value) {
        int[] parts = parse(value, "BS");
        YearData year = year(parts[0]);
        if (parts[1] < 1 || parts[1] > 12) {
            throw new DateRangeNotSupported("BS month must be between 1 and 12.");
        }
        int maxDay = year.months[parts[1] - 1];
        if (parts[2] < 1 || parts[2] > maxDay) {
            throw new InvalidBsDayOfMonthException(
                    "Day " + parts[2] + " is not valid for month " + parts[1]
                            + " of Nepali year " + parts[0] + ". This month only has " + maxDay + " days.");
        }
        int offset = parts[2] - 1;
        for (int month = 0; month < parts[1] - 1; month++) {
            offset += year.months[month];
        }
        return year.newYear.plusDays(offset).toString();
    }

    public static String adToBs(String value) {
        int[] parts = parse(value, "AD");
        final LocalDate ad;
        try {
            ad = LocalDate.of(parts[0], parts[1], parts[2]);
        } catch (RuntimeException e) {
            throw new InvalidDateFormatException("Invalid AD date: " + value + ". Expected format yyyy-MM-dd.");
        }

        for (int index = YEARS.size() - 1; index >= 0; index--) {
            YearData year = YEARS.get(index);
            if (!ad.isBefore(year.newYear)) {
                long remaining = java.time.temporal.ChronoUnit.DAYS.between(year.newYear, ad);
                for (int month = 0; month < 12; month++) {
                    if (remaining < year.months[month]) {
                        return String.format("%04d/%02d/%02d", year.year, month + 1, remaining + 1);
                    }
                    remaining -= year.months[month];
                }
                break;
            }
        }
        throw new DateRangeNotSupported("AD date is outside the supported calendar range: " + value);
    }

    public static int daysInMonth(int year, int month) {
        if (month < 1 || month > 12) {
            return 0;
        }
        try {
            return year(year).months[month - 1];
        } catch (DateRangeNotSupported e) {
            return 0;
        }
    }

    private static int[] parse(String value, String calendar) {
        if (value == null) {
            throw new InvalidDateFormatException(calendar + " date is required.");
        }
        Matcher matcher = DATE.matcher(value.trim());
        if (!matcher.matches()) {
            throw new InvalidDateFormatException("Invalid " + calendar + " date: " + value + ". Expected format yyyy-MM-dd.");
        }
        return new int[]{Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)), Integer.parseInt(matcher.group(3))};
    }

    private static YearData year(int year) {
        for (YearData data : YEARS) {
            if (data.year == year) {
                return data;
            }
        }
        throw new DateRangeNotSupported("Supported BS years are " + YEARS.get(0).year + "-" + YEARS.get(YEARS.size() - 1).year + ".");
    }

    private static List<YearData> loadYears() {
        List<YearData> result = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                NepaliDateConverter.class.getResourceAsStream("/nepali_dates.csv"), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank() || line.startsWith("#")) {
                    continue;
                }
                String[] fields = line.split(",");
                int[] months = new int[12];
                for (int index = 0; index < months.length; index++) {
                    months[index] = Integer.parseInt(fields[index + 2]);
                }
                result.add(new YearData(Integer.parseInt(fields[0]), LocalDate.parse(fields[1]), months));
            }
        } catch (IOException | RuntimeException e) {
            throw new ExceptionInInitializerError(e);
        }
        return List.copyOf(result);
    }

    private record YearData(int year, LocalDate newYear, int[] months) {
    }
}
