package utils;

public class Point {
    public double x;
    public double y;
    public CardinalPoint cardinality;

    public static Point translate(Point oldPoint, double delatX, double deltaY) {
        return new Point(oldPoint.x + delatX, oldPoint.y + deltaY);
    }

    public static Point diff(Point pt1, Point pt2) {
        return pt1.diff(pt2);
    }

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Point(double x, double y, CardinalPoint cardinality) {
        this.x = x;
        this.y = y;
        this.cardinality = cardinality;
    }

    public boolean isSame(Point other) {
        return (this.x == other.x) && (this.y == other.y);
    }

    public boolean isInRange(Point other, double tolerance) {
        Point absoluteDiff = this.diff(other).abs();
        return absoluteDiff.x < tolerance && absoluteDiff.y < tolerance;
    }

    public Point diff(Point other) {
        return new Point(this.x - other.x, this.y - other.y);
    }

    public Point abs() {
        return new Point(Math.abs(x), Math.abs(y));
    }

    public Point deepCpy() {
        return new Point(x, y);
    }
}
