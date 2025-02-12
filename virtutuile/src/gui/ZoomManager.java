package gui;

import utils.Point;

public class ZoomManager {

    private double pixelsPerMeters = 100;
    private double currentScale = 1;

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

    public void zoomBy(double zoomFactor) {
        this.currentScale *= zoomFactor;
    }

    public void resetZoom() {
        this.currentScale = 1;
    }

    public double getCurrentScale() {
        return this.currentScale;
    }

    public double metersToInch(double pMeters) {
        return pMeters * 39.3701;
    }

    public double inchToMeters(double pInch) {
        return pInch / 39.3701;
    }
}
