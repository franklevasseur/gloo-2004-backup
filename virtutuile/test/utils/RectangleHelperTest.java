package utils;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RectangleHelperTest {

    @Test
    public void rectangleInfoToSummit_shouldWork() {
        // arrange
        List<Point> summits = new ArrayList<>();
        summits.add(new Point(0, 0));
        summits.add(new Point(0, 10));
        summits.add(new Point(5, 10));
        summits.add(new Point(5, 0));

        // act
        RectangleInfo rectangleInfo = RectangleHelper.summitsToRectangleInfo(summits);

        // assert
        assertEquals(rectangleInfo.width, 5);
        assertEquals(rectangleInfo.height, 10);
        assertEquals(rectangleInfo.topLeftCorner.x, 0);
        assertEquals(rectangleInfo.topLeftCorner.y, 0);
    }

    @Test
    public void summitsToRectangleInfo_shouldWork() {
        // arrange
        RectangleInfo rect = new RectangleInfo(new Point(0, 0), 5, 10);

        // act
        List<Point> summits = RectangleHelper.rectangleInfoToSummits(rect);

        // assert
        assertTrue(new Point(0, 0).isSame(summits.get(0)));
        assertTrue(new Point(5, 0).isSame(summits.get(1)));
        assertTrue(new Point(5, 10).isSame(summits.get(2)));
        assertTrue(new Point(0, 10).isSame(summits.get(3)));
    }
}