package utils;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ShapeHelperTest {

    @Test
    public void allOutsideWhenAllSummitsAreOnBorders() {
        // arrange
        List<Point> bigShapeSummits = new ArrayList<>();
        bigShapeSummits.add(new Point(0, 0));
        bigShapeSummits.add(new Point(0, 10));
        bigShapeSummits.add(new Point(10, 10));
        bigShapeSummits.add(new Point(10, 0));

        AbstractShape big = new AbstractShape(bigShapeSummits);

        List<Point> smallShapeSummits = new ArrayList<>();
        smallShapeSummits.add(new Point(3, 0));
        smallShapeSummits.add(new Point(3, 10));
        smallShapeSummits.add(new Point(7, 10));
        smallShapeSummits.add(new Point(7, 0));

        AbstractShape small = new AbstractShape(smallShapeSummits);

        // act
        boolean actual = ShapeHelper.isAllOutside(small, big);

        // assert
        assertFalse(actual);
    }
}