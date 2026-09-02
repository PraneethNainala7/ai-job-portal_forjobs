package com.aijobportal.ai.matching.experience;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ExperienceTextParser {

    private static final Pattern RANGE_PATTERN = Pattern.compile(
            "(\\d+)\\s*(?:\\+|\\s*(?:-|to)\\s*(\\d+))?\\+?\\s*(?:years?|yrs?)",
            Pattern.CASE_INSENSITIVE
    );

    private ExperienceTextParser() {
    }

    public enum ParseMode {
        MIN_YEARS,
        MAX_YEARS
    }

    public static int parseYears(String value, ParseMode mode) {
        if (value == null || value.isBlank()) {
            return 0;
        }
        Matcher matcher = RANGE_PATTERN.matcher(value.toLowerCase(Locale.ROOT));
        if (!matcher.find()) {
            return 0;
        }
        int first = Integer.parseInt(matcher.group(1));
        String secondGroup = matcher.group(2);
        if (secondGroup == null || secondGroup.isBlank()) {
            return first;
        }
        int second = Integer.parseInt(secondGroup);
        int min = Math.min(first, second);
        int max = Math.max(first, second);
        return mode == ParseMode.MIN_YEARS ? min : max;
    }
}
