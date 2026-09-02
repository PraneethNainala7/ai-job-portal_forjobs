package com.aijobportal.ai.matching.experience;

import org.junit.jupiter.api.Test;

import static com.aijobportal.ai.matching.experience.ExperienceTextParser.ParseMode;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ExperienceTextParserTest {

    @Test
    void parsesSingleValueForBothModes() {
        assertEquals(3, ExperienceTextParser.parseYears("3+ years", ParseMode.MIN_YEARS));
        assertEquals(3, ExperienceTextParser.parseYears("3+ years", ParseMode.MAX_YEARS));
    }

    @Test
    void parsesRangeUsingMinForJobRequirement() {
        assertEquals(2, ExperienceTextParser.parseYears("2-4 years", ParseMode.MIN_YEARS));
    }

    @Test
    void parsesRangeUsingMaxForCandidateBenefit() {
        assertEquals(4, ExperienceTextParser.parseYears("2-4 years", ParseMode.MAX_YEARS));
    }

    @Test
    void parsesToKeywordRange() {
        assertEquals(2, ExperienceTextParser.parseYears("2 to 4 years", ParseMode.MIN_YEARS));
        assertEquals(4, ExperienceTextParser.parseYears("2 to 4 years", ParseMode.MAX_YEARS));
    }
}
