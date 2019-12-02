package utils;

import java.util.ArrayList;
import java.util.List;

public class Segment {

    public Point pt1;
    public Point pt2;

    public Segment(Point pt1, Point pt2) {
        this.pt1 = pt1;
        this.pt2 = pt2;
    }

    public boolean isSame(Segment other) {
        return pt1.isSame(other.pt1) && pt2.isSame(other.pt2);
    }

    public boolean isSameOrOpposite(Segment other) {
        return isSame(other) || isSame(new Segment(other.pt2, other.pt1));
    }

    public boolean doesIntersect(Segment other, double tolerance) {
        return intersect(other, tolerance) != null;
    }

    public Point intersect(Segment other, double tolerance) {
        Point theoricalIntersection = this.getTheoricalIntersection(other);
        return (contains(theoricalIntersection, tolerance) && other.contains(theoricalIntersection, tolerance)) ? theoricalIntersection : null;
    }

    public boolean barelyTouches(Segment other, double tolerance) {
        return this.contains(other.pt1, tolerance)
                || this.contains(other.pt2, tolerance)
                || other.contains(this.pt1, tolerance)
                || other.contains(this.pt2, tolerance);
    }

    public Point extendAndIntersect(Segment other, double tolerance) {
        Point theoricalIntersection = this.getTheoricalIntersection(other);
        return other.contains(theoricalIntersection, tolerance) ? theoricalIntersection : null;
    }

    private Point getTheoricalIntersection(Segment other) {
        if (sameSlope(other)) {
            return null;
        }

        if (this.getSlope() == Double.POSITIVE_INFINITY
                || this.getSlope() == Double.NEGATIVE_INFINITY) {
            double x = this.pt1.x;
            double y = other.predictY(x);
            return new Point(x, y);
        }

        if (other.getSlope() == Double.POSITIVE_INFINITY
                || other.getSlope() == Double.NEGATIVE_INFINITY) {
            double x = other.pt1.x;
            double y = this.predictY(x);
            return new Point(x, y);
        }

        double x = (other.getYAxisIntercept() - this.getYAxisIntercept()) / (this.getSlope() - other.getSlope());
        double y = predictY(x);
        return new Point(x, y);
    }

    public boolean sameSlope(Segment other) {
        double thisSlope = getSlope();
        double otherSlope = other.getSlope();
        return thisSlope == otherSlope // TODO: might break because of rounding errors
                || ((thisSlope == Double.NEGATIVE_INFINITY || thisSlope == Double.POSITIVE_INFINITY)
                && (otherSlope == Double.NEGATIVE_INFINITY || otherSlope == Double.POSITIVE_INFINITY));
    }

    public boolean contains(Point p) {
        return this.contains(p,0);
    }

    public boolean contains(Point p, double tolerance) {
        if (p == null) {
            return false;
        }

        if (this.getSlope() == Double.POSITIVE_INFINITY
                || this.getSlope() == Double.NEGATIVE_INFINITY) {
            return p.x == pt1.x
                    && p.y <= Math.max(pt1.y, pt2.y) + tolerance
                    && p.y >= Math.min(pt1.y, pt2.y) - tolerance;
        }

        // really fucking important condition !!
        if (this.getSlope() == 0) {
            return Math.abs(p.y - pt1.y) < tolerance
                    && p.x <= Math.max(pt1.x, pt2.x)
                    && p.x >= Math.min(pt1.x, pt2.x);
        }

        return isInLine(p, tolerance)
            && p.x <= Math.max(pt1.x, pt2.x) + tolerance
            && p.x >= Math.min(pt1.x, pt2.x) - tolerance
            && p.y <= Math.max(pt1.y, pt2.y) + tolerance
            && p.y >= Math.min(pt1.y, pt2.y) - tolerance;
    }

    private boolean isInLine(Point p, double tolerance) {
        return Math.abs(p.y - predictY(p.x)) <= tolerance;
    }

    public double predictY(double x) {
        if (getSlope() == 0) {
            return pt1.y;
        }
        return (getSlope() * x) + getYAxisIntercept();
    }

    public double getSlope() {
        return (pt2.y - pt1.y) / (pt2.x - pt1.x);
    }

    public double getYAxisIntercept() {
        return pt1.y - (getSlope() * pt1.x);
    }

    public static List<Segment> fromPoints(List<Point> summits) {
        List<Segment> segments = new ArrayList<>();

        int tail = 0;
        int head = tail + 1;
        for (Point s : summits) {
            Segment newSegment = new Segment(summits.get(tail), summits.get(head));
            segments.add(newSegment);

            tail++;
            head = head == summits.size() - 1 ? 0 : head + 1;
        }

        return segments;
    }

    public boolean isInside(List<Segment> outline, boolean includeBorders) {
        return pt1.isInsideSegments(outline, includeBorders) && pt2.isInsideSegments(outline, includeBorders);
    }
}
