package utils;

import java.util.ArrayList;
import java.util.List;

public class Point {
    public double x;
    public double y;
    public CardinalPoint cardinality;

    public static Point translate(Point oldPoint, double delatX, double deltaY) {
        return oldPoint.translate(new Point(delatX, deltaY));
    }

    public static Point diff(Point pt1, Point pt2) {
        return pt1.diff(pt2);
    }

    public static List<Point> fromSegments(List<Segment> segments) {
        List<Point> summits = new ArrayList<>();
        for (Segment seg : segments) {
            if (summits.stream().allMatch(s -> !s.isSame(seg.pt1))) {
                summits.add(seg.pt1);
            }
            if (summits.stream().allMatch(s -> !s.isSame(seg.pt2))) {
                summits.add(seg.pt2);
            }
        }
        return summits;
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

    public Point translate(Point other) {
        return new Point(x + other.x, y + other.y);
    }

    public Point abs() {
        return new Point(Math.abs(x), Math.abs(y));
    }

    public Point deepCpy() {
        return new Point(x, y);
    }

    public boolean isInside(List<Point> outline) {
        Segment currentPointExtendedToInfinity = new Segment(this, new Point(Double.POSITIVE_INFINITY, this.y));
        List<Segment> outlineSegments = Segment.toSegments(outline);

        int interSectionCount = 0;
        for (Segment segment: outlineSegments) {
            if (segment.isElementOf(this)) {
                return true; // point is exactly on edge
            }

            if (segment.doesIntersect(currentPointExtendedToInfinity)) {
                interSectionCount++;
            }
        }

        return (interSectionCount % 2 == 1);
    }
}
