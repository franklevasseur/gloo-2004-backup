package Domain;

import utils.CardinalPoint;

public class Point {
    private Measure x;
    private Measure y;
    private CardinalPoint cardinality = null;

    public Point(Measure pX, Measure pY){
        this.x = pX;
        this.y = pY;
    }

    public Point(utils.Point point) {
        this.x = new Measure(point.x, UnitType.m);
        this.y = new Measure(point.y, UnitType.m);
    }

    public Point(Measure  x, Measure y, CardinalPoint cardinality) {
        this.x = x;
        this.y = y;
        this.cardinality = cardinality;
    }

    public Measure getX() {
        return x;
    }

    public Measure getY() {
        return y;
    }

    public void setX(Measure x) {
        this.x = x;
    }

    public void setY(Measure y) {
        this.y = y;
    }

    public CardinalPoint getCardinality() {
        return cardinality;
    }

    public void setCardinality(CardinalPoint cardinality) {
        this.cardinality = cardinality;
    }

    public static Point translate(Point oldPoint, double delatX, double deltaY) {

        Measure tempX = new Measure(oldPoint.x.getValue() + delatX);
        Measure tempY = new Measure(oldPoint.y.getValue() + deltaY);
        return new Point(tempX, tempY);
    }
}
