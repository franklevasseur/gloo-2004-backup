package utils;

import java.util.ArrayList;
import java.util.List;

public class Point {
    public double x;
    public double y;
    public CardinalPoint cardinality;

    public static double DOUBLE_TOLERANCE = 0.001;

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

    public boolean isInside(List<Point> outline, boolean includeBorder) {
        return isInsideSegments(Segment.toSegments(outline), includeBorder);
    }

    public boolean isInsideSegments(List<Segment> outlineSegments, boolean includeBorder) {
        Segment currentPointExtendedToInfinity = new Segment(this, new Point(Double.POSITIVE_INFINITY, this.y));

        List<Point> intersections = new ArrayList<>();
        for (Segment segment: outlineSegments) {
            if (segment.contains(this, DOUBLE_TOLERANCE)
                    || segment.pt1.isInRange(this, DOUBLE_TOLERANCE)
                    || segment.pt2.isInRange(this, DOUBLE_TOLERANCE)) {
                return includeBorder; // point is exactly on edge
            }

            Point intersection = segment.intersect(currentPointExtendedToInfinity, DOUBLE_TOLERANCE);
            boolean intersectionIsNew = intersection != null && intersections.stream().noneMatch(i -> i.isSame(intersection));
            boolean intersectionIsNotAnEdge = intersection != null
                    && !(segment.pt1.isInRange(intersection, DOUBLE_TOLERANCE)
                    || segment.pt2.isInRange(intersection, DOUBLE_TOLERANCE));

            if (intersectionIsNew && intersectionIsNotAnEdge) {
                intersections.add(intersection);
            }
        }

        return (intersections.size() % 2 == 1);
    }
}
