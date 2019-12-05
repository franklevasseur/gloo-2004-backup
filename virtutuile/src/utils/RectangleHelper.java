package utils;

import Domain.Material;
import javafx.scene.shape.Rectangle;

import java.util.*;

public class RectangleHelper {

    public static List<Point> rectangleInfoToSummits(RectangleInfo rect) {
        return rectangleInfoToSummits(rect.topLeftCorner, rect.width, rect.height);
    }

    public static List<Point> rectangleInfoToSummits(Point topLeftCorner, double width, double height) {

        double x = topLeftCorner.x;
        double y = topLeftCorner.y;

        topLeftCorner.cardinality = CardinalPoint.NW;
        Point topRightCorner = new Point(x + width, y, CardinalPoint.NE);
        Point bottomLeftCorner = new Point(x, y + height, CardinalPoint.SW);
        Point bottomRightCorner = new Point(x + width, y + height, CardinalPoint.SE);
        return Arrays.asList(topLeftCorner, topRightCorner, bottomRightCorner, bottomLeftCorner);
    }

    public static List<Point> getClockWise(List<Point> summits) {
        return rectangleInfoToSummits(summitsToRectangleInfo(summits));
    }

    public static RectangleInfo summitsToRectangleInfo(List<Point> summits) {

        if (!isARectangle(summits)) {
            throw new RectangleError("Yoooo, c'est pas un rectangle que tu me donne mon gars...");
        }

        Set<Double> xs = new HashSet<Double>();
        Set<Double> ys = new HashSet<Double>();
        for (Point summit: summits) {
            xs.add(summit.x);
            ys.add(summit.y);
        }

        double leftX = Collections.min(xs);
        double rightX = Collections.max(xs);
        double topY = Collections.min(ys);
        double bottomY = Collections.max(ys);

        double width = rightX - leftX;
        double height = bottomY - topY;

        return new RectangleInfo(new Point(leftX, topY), width, height);
    }

    public static RectangleOrientation getOrientation(RectangleInfo rectangleInfo) {
        return rectangleInfo.width > rectangleInfo.height ? RectangleOrientation.HORIZONTAL
                : (rectangleInfo.height > rectangleInfo.width ? RectangleOrientation.VERTICAL
                : RectangleOrientation.NONE);
    }

    public static RectangleOrientation getOrientation(List<Point> summits) {
        return getOrientation(summitsToRectangleInfo(summits));
    }

    public static List<Point> flip(List<Point> summits) {
        RectangleInfo rectangleInfo = summitsToRectangleInfo(summits);
        double tmp = rectangleInfo.width;
        rectangleInfo.width = rectangleInfo.height;
        rectangleInfo.height = tmp;
        return rectangleInfoToSummits(rectangleInfo);
    }

    public static boolean isARectangle(List<Point> summits) {
        if (summits.size() != 4) {
            return false;
        }

        HashSet<Double> xs = new HashSet<Double>();
        HashSet<Double> ys = new HashSet<Double>();
        for (Point summit: summits) {
            xs.add(summit.x);
            ys.add(summit.y);
        }

        return xs.size() == 2 && ys.size() == 2;
    }

    public static boolean isInclinedRectangle(List<Point> summits) {
        return !isARectangle(summits) && areSegmentsARectangle(Segment.fromPoints(summits));
    }

    public static boolean areSegmentsARectangle(List<Segment> segments) {
        if (segments.size() != 4) {
            return false;
        }

        for (int i = 0; i < 4; i++) {

            int tail = i;
            int head = (i + 1) % 4;

            Segment tailSegment = segments.get(tail);
            Segment headSegment = segments.get(head);

            if (!tailSegment.isPerpendicular(headSegment, Point.DOUBLE_TOLERANCE)
                    || !tailSegment.pt2.isSame(headSegment.pt1)) {
                return false;
            }
        }

        return true;
    }

    public static Point getTopLeft(Point point1, Point point2) {
        double minX = point1.x < point2.x ? point1.x : point2.x;
        double minY = point1.y < point2.y ? point1.y : point2.y;
        return new Point(minX, minY);
    }
}
