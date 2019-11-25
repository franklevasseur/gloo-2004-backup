package utils;

import java.util.*;

public class RectangleHelper {

    public static List<Point> rectangleInfoToSummits(double topLeftCornerX, double topLeftCornerY, double width, double height) {
        Point topLeftCorner = new Point(topLeftCornerX, topLeftCornerY);
        return rectangleInfoToSummits(topLeftCorner, width, height);
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

    public static Point getTopLeft(Point point1, Point point2) {
        double minX = point1.x < point2.x ? point1.x : point2.x;
        double minY = point1.y < point2.y ? point1.y : point2.y;
        return new Point(minX, minY);
    }
}
