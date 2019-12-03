package utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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

    public static boolean isAllInside(AbstractShape small, AbstractShape big) {
        return small.summits.stream().allMatch(s -> s.isInside(big.summits, true));
    }

    public static boolean isAllOutside(AbstractShape small, AbstractShape big) {
        if (areSame(small, big) || allSummitsAreOnBorder(small, big)) {
            return false;
        }

        return small.summits.stream().allMatch(s -> !s.isInside(big.summits, false));
    }

    public static boolean allSummitsAreOnBorder(AbstractShape small, AbstractShape big) {
        List<Segment> bigBorders = Segment.fromPoints(big.summits);

        boolean allSummitsAreOnBorder = small.summits.stream().allMatch(
                su -> bigBorders.stream().filter(
                        seg -> seg.contains(su, Point.DOUBLE_TOLERANCE)).collect(Collectors.toList()).size() > 0);
        return allSummitsAreOnBorder;
    }

    private static Point getMiddle(AbstractShape shape) {
        double minX = getMinX(shape).x;
        double minY = getMinY(shape).y;
        double maxX = getMaxX(shape).x;
        double maxY = getMaxY(shape).y;

        double middleX = (maxX + minX) / 2;
        double middleY = (maxY + minY) / 2;

        return new Point(middleX, middleY);
    }

    public static boolean areSame(AbstractShape shape1, AbstractShape shape2) {
        if (shape1.summits.size() != shape2.summits.size()) {
            return false;
        }

        int index = 0;
        for (Point summit : shape1.summits) {
            if (!summit.isSame(shape2.summits.get(index))) {
                return false;
            }
        }

        return true;
    }

    public static List<Point> getFlattedSummits(List<AbstractShape> shapes) {
        return shapes.stream().flatMap(s -> s.summits.stream()).collect(Collectors.toList());
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
