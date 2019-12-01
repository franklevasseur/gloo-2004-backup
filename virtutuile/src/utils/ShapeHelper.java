package utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ShapeHelper {

    public static double getWidth(AbstractShape shape) {
        return getMaxX(shape).x - getMinX(shape).x;
    }

    public static double getHeight(AbstractShape shape) {
        return getMaxY(shape).y - getMinY(shape).y;
    }

    public static Point getTopLeftCorner(AbstractShape shape) {
        double minX = getMinX(shape).x;
        return shape.summits.stream().filter(p -> p.x == minX).min(Comparator.comparing(s -> s.y)).get();
    }

    // gives a point that might not be a summit of the shape, but only the boxing top left summit
    public static Point getTheoricalTopLeftCorner(AbstractShape shape) {
        double minX = getMinX(shape).x;
        double minY = getMinY(shape).y;
        return new Point(minX, minY);
    }

    public static List<Point> simplifySummits(AbstractShape shape) {
        List<Point> resultantSummits = new ArrayList<>();
        for (int i = 0; i < shape.summits.size(); i++) {
            int tail = i - 1 < 0 ? shape.summits.size() - 1 : i - 1;
            int current = i % shape.summits.size();
            int head = (i + 1) % shape.summits.size();

            Segment segment = new Segment(shape.summits.get(tail), shape.summits.get(head));
            if (!segment.contains(shape.summits.get(current))) {
                resultantSummits.add(shape.summits.get(current));
            }
        }
        return resultantSummits;
    }

    private static Point getMinX(AbstractShape shape) {
        return Collections.min(shape.summits, Comparator.comparing(p -> p.x));
    }

    private static Point getMaxX(AbstractShape shape) {
        return Collections.max(shape.summits, Comparator.comparing(p -> p.x));
    }

    private static Point getMinY(AbstractShape shape) {
        return Collections.min(shape.summits, Comparator.comparing(p -> p.y));
    }

    private static Point getMaxY(AbstractShape shape) {
        return Collections.max(shape.summits, Comparator.comparing(p -> p.y));
    }
}
