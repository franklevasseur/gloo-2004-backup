package Domain;


import utils.Color;

public class SealsInfo {
    private Measure width;
    private Color color;

    public SealsInfo(Measure pWidth, Color color){
        width = pWidth;
        this.color = color;
    }

    public Color getColor() {
        return color;
    }

    public Measure getWidth() {
        return width;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public void setWidth(Measure width) {
        this.width = width;
    }
}
