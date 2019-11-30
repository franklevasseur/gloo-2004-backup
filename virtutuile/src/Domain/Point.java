package Domain;

import utils.CardinalPoint;

public class Point {
    private double x;
    private double y;
    private CardinalPoint cardinality = null;

    public Point(double pX, double pY){
        this.x = pX;
        this.y = pY;
    }

    public Point(utils.Point point) {
        this.x = point.x;
        this.y = point.y;
    }

    public Point(double  x, double y, CardinalPoint cardinality) {
        this.x = x;
        this.y = y;
        this.cardinality = cardinality;
    }

    public utils.Point toAbstract() {
        return new utils.Point(this.x, this.y);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public CardinalPoint getCardinality() {
        return cardinality;
    }

    public void setCardinality(CardinalPoint cardinality) {
        this.cardinality = cardinality;
    }

    public static Point translate(Point oldPoint, double delatX, double deltaY) {

        double tempX = oldPoint.x + delatX;
        double tempY = oldPoint.y + deltaY;
        return new Point(tempX, tempY);
    }
}
