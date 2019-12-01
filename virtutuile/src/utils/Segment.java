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

    public boolean doesIntersect(Segment other) {
        return intersect(other) != null;
    }

    public Point intersect(Segment other) {
        Point theoricalIntersection = this.getTheoricalIntersection(other);
        return (contains(theoricalIntersection) && other.contains(theoricalIntersection)) ? theoricalIntersection : null;
    }

    public Point getTheoricalIntersection(Segment other) {
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

        double x = (other.getIntercept() - this.getIntercept()) / (this.getSlope() - other.getSlope());
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
        if (p == null) {
            return false;
        }

        if (this.getSlope() == Double.POSITIVE_INFINITY
                || this.getSlope() == Double.NEGATIVE_INFINITY) {
            return p.x == pt1.x
                    && p.y <= Math.max(pt1.y, pt2.y)
                    && p.y >= Math.min(pt1.y, pt2.y);
        }

        return isInLine(p)
            && p.x <= Math.max(pt1.x, pt2.x)
            && p.x >= Math.min(pt1.x, pt2.x)
            && p.y <= Math.max(pt1.y, pt2.y)
            && p.y >= Math.min(pt1.y, pt2.y);
    }

    private boolean isInLine(Point p) {
        return p.y == predictY(p.x); // TODO: might break because of rounding errors
    }

    private double predictY(double x) {
        return (getSlope() * x) + getIntercept();
    }

    private double getSlope() {
        return (pt2.y - pt1.y) / (pt2.x - pt1.x);
    }

    private double getIntercept() {
        return pt1.y - (getSlope() * pt1.x);
    }

    public static List<Segment> toSegments(List<Point> summits) {
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

    public static List<Point> findIntersection(Segment segment, List<Segment> tileSegments) {
        return findIntersection(segment, tileSegments, false);
    }

    public static List<Point> findTheoricalIntersection(Segment segment, List<Segment> tileSegments) {
        return findIntersection(segment, tileSegments, true);
    }

    private static List<Point> findIntersection(Segment segment, List<Segment> tileSegments, boolean extendSegment) {
        List<Point> currentIntersections = new ArrayList<>();
        for (Segment seg: tileSegments) {
            Point intersection = extendSegment ? seg.getTheoricalIntersection(segment) : seg.intersect(segment);
            if (intersection != null && currentIntersections.stream().noneMatch(i -> i.isSame(intersection))) {
                currentIntersections.add(intersection);
            }
        }
        return currentIntersections;
    }

    public boolean isInside(List<Segment> outline, boolean includeBorders) {
        return pt1.isInsideSegments(outline, includeBorders) && pt2.isInsideSegments(outline, includeBorders);
    }
}
