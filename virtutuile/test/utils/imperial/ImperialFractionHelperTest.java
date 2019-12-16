package utils.imperial;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ImperialFractionHelperTest {

    ImperialFractionHelper helper = new ImperialFractionHelper();

    @Test
    void parseImperialFraction() {
        // arrange & act
        double actual = helper.parseImperialFraction("13' 10\" 3 / 4");

        // assert
        double expected = (13 * 12) + 10.75;
        assertEquals(expected, actual);
    }

    @Test
    void parseImperialFraction_withOnlyInchesIndicator_shouldStillWork() {
        // arrange & act
        double actual = helper.parseImperialFraction("10\" 3 / 4");

        // assert
        double expected = 10.75;
        assertEquals(expected, actual);
    }

    @Test
    void parseImperialFraction_withNoIndicator_shouldStillWork() {
        // arrange & act
        double fraction = helper.parseImperialFraction("3 / 4");
        double plainInt = helper.parseImperialFraction("69");

        // assert
        assertEquals(0.75, fraction);
        assertEquals(69, plainInt);
    }

    @Test
    void formatImperialFraction_withSimpleFraction_shouldDecomposeFraction() {
        // arrange & act
        String actual = helper.formatImperialFraction(14.25);

        // assert
        String expected = "1'2\"1/4";
        assertEquals(expected, actual);
    }

    @Test
    void formatImperialFraction_withWeirdFraction_shouldStillDecomposeFraction() {
        // arrange & act
        String actual = helper.formatImperialFraction(3.1428571429);

        // assert
        String expected = "3\"9/64";
        assertEquals(expected, actual);
    }

    @Test
    void formatImperialFraction_withLessThanOne_shouldStillDecomposeFraction() {
        // arrange & act
        String actual = helper.formatImperialFraction(0.35);

        // assert
        String expected = "11/32\"";
        assertEquals(expected, actual);
    }

    @Test
    void formatImperialFraction_withZero_shouldReturnString0() {
        // arrange & act
        String actual = helper.formatImperialFraction(0.0);

        // assert
        String expected = "0";
        assertEquals(expected, actual);
    }

    @Test
    void formatImperialFraction_withAlmostInteger_shouldReturnInteger() {
        // arrange & act
        String actual = helper.formatImperialFraction(7.0000001);

        // assert
        String expected = "7\"";
        assertEquals(expected, actual);
    }
}