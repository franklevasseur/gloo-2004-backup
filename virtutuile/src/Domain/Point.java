package Domain;

public class Point {
    private Measure x;
    private Measure y;

    public Point(Measure pX, Measure pY){
        this.x = pX;
        this.y = pY;
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

}
