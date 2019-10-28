package utils;

public class Point {
    public double x;
    public double y;
    public CardinalPoint cardinality;

    public static Point translate(Point oldPoint, double delatX, double deltaY) {
        return new Point(oldPoint.x + delatX, oldPoint.y + deltaY);
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
}
