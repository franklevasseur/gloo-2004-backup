package utils.imperial;

import java.math.BigDecimal;
import java.math.MathContext;

public class ImperialFractionHelper {

    public double parseImperialFraction(String initialText) {

        ImperialFractionText imperialFraction = extractFeetsAndInches(initialText);

        double feets = fractionToDouble(imperialFraction.feets);
        double inches;
        if (isFraction(imperialFraction.inches)) {
            inches = fractionToDouble(imperialFraction.inches);
        } else if (imperialFraction.inches.length() == 0) {
            inches = 0;
        } else {
            inches = Double.parseDouble(imperialFraction.inches);
        }

        double reminder = fractionToDouble(imperialFraction.reminder);

        return (feets * 12) + inches + reminder;
    }

    public String formatImperialFraction(double decimalInches) {

        int feet = (int) decimalInches / 12;
        int inches = (int) decimalInches % 12;

        if (decimalInches == 0) {
            return "0";
        }

        double reminder = decimalInches % 1;
        Fraction fraction = decimalToFraction(reminder);

        String out = "";
        if (feet != 0) {
            out += feet + "'";
        }

        if (inches != 0) {
            out += inches + "\"";
        }

        if (fraction.isValid()) {
            out += fraction.format();
            out += inches == 0 ? "\"" : "";
        }
        return out;
    }

    public String formatImperialFractionUsingBaseTwoDenominator(double nInches) {
        // TODO: code this
        return "";
    }

    private ImperialFractionText extractFeetsAndInches(String initialText) {
        String[] splitByFeetIndicator = initialText.split("'", 2);
        String[] splitByInchesIndicator;

        String feet;
        String inches;
        String reminder;

        if (splitByFeetIndicator.length > 1) {
            feet = splitByFeetIndicator[0];
            splitByInchesIndicator = splitByFeetIndicator[1].split("\"", 2);
        } else if (initialText.contains("'") && splitByFeetIndicator.length == 0) {
            feet = splitByFeetIndicator[0];
            return new ImperialFractionText(feet, "", "");
        } else {
            splitByInchesIndicator = splitByFeetIndicator[0].split("\"", 2);
            feet = "0";
        }

        if (splitByInchesIndicator.length > 1) {
            inches = splitByInchesIndicator[0];
            reminder = splitByInchesIndicator[1];
        } else if (initialText.contains("\"") && splitByInchesIndicator.length == 0) {
            inches = splitByInchesIndicator[0];
            reminder = "";
        } else {
            inches = "";
            reminder = splitByInchesIndicator[0];
        }
        return new ImperialFractionText(feet, inches, reminder);
    }

    private Double fractionToDouble(String fraction) {
        Double value = null;
        if (fraction.length() > 0) {
            if (fraction.contains("/")) {
                String[] numbers = fraction.split("/");
                if (numbers.length == 2) {
                    BigDecimal d1 = BigDecimal.valueOf(Double.valueOf(numbers[0]));
                    BigDecimal d2 = BigDecimal.valueOf(Double.valueOf(numbers[1]));
                    BigDecimal response = d1.divide(d2, MathContext.DECIMAL128);
                    value = response.doubleValue();
                }
            } else {
                value = Double.parseDouble(fraction);
            }
        } else {
            value = 0.0;
        }
        return value;
    }

    private Fraction decimalToFraction(double decimal) {
        if (decimal < 0) {
            Fraction positiveFraction = decimalToFraction((-1) * decimal);
            return positiveFraction.times(-1);
        }

        String s = String.valueOf(decimal);
        int digitsDec = s.length() - 1 - s.indexOf('.');

        float unsimplifiedDenominator = (float) Math.pow(10, digitsDec);
        float unsimplifiedNumerator = (float) Math.round(decimal * unsimplifiedDenominator);

        float gcd = getGCD(unsimplifiedNumerator, unsimplifiedDenominator);
        float numerator = unsimplifiedNumerator / gcd;
        float denominator = unsimplifiedDenominator / gcd;
        return new Fraction((int) numerator, (int) denominator);
    }

    private float getGCD(float n1, float n2) {
        while (n1 != n2) {
            if (n1 > n2)
                n1 -= n2;
            else
                n2 -= n1;
        }
        return n1;
    }

    private boolean isFraction(String pValue) {
        return pValue.contains("/");
    }
}
