package utils.imperial;

public class Fraction {

    public int numerator;
    public int denominator;

    public Fraction(int numerator, int denominator) {
        this.denominator = denominator;
        this.numerator = numerator;
    }

    public boolean isValid() {
        return denominator != 0;
    }

    public String format() {
        return String.format("%d/%d", this.numerator, this.denominator);
    }
}
