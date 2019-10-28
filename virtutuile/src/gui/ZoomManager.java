package gui;

import utils.Point;

public class ZoomManager {

    private double pixelsPerMeters = 30;

    public double pixelsToMeters(double pixels) {
        return pixels / pixelsPerMeters;
    }

    public Point pixelsToMeters(Point pixelsPoint) {
        double x = this.pixelsToMeters(pixelsPoint.x);
        double y = this.pixelsToMeters(pixelsPoint.y);
        return new Point(x, y);
    }

    public double metersToPixels(double meters) {
        return meters * pixelsPerMeters;
    }

    public Point metersToPixels(Point metersPoint) {
        double x = this.metersToPixels(metersPoint.x);
        double y = this.metersToPixels(metersPoint.y);
        return new Point(x, y);
    }
}
