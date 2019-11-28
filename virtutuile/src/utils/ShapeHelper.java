package utils;

import java.util.Collections;
import java.util.Comparator;

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
