package Domain;


import utils.Color;

import java.io.Serializable;

public class SealsInfo implements Serializable {
    private double width;
    private Color color;

    public SealsInfo(double pWidth, Color color) {
        width = pWidth;
        this.color = color;
    }

    public Color getColor() {
        return color;
    }

    public double getWidth() {
        return width;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public void setWidth(double width) {
        this.width = width;
    }
}
