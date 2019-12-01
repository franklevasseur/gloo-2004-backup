package utils;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ShapeHelperTest {

    @Test
    void simplifySummits() {
        // arrange
        Point topLeft = new Point(0, 10);
        Point topRight = new Point(10, 10);
        Point bottomRight = new Point(10, 0);
        Point bottomLeft = new Point(0, 0);

        Point redundant = new Point(0, 5);

        List<Point> summits = new ArrayList<>();
        summits.add(topLeft);
        summits.add(topRight);
        summits.add(bottomRight);
        summits.add(bottomLeft);
        summits.add(redundant);

        AbstractShape shape = new AbstractShape(summits);

        // act
        List<Point> simplifiedPoints = ShapeHelper.simplifySummits(shape);

        // assert
        assertEquals(simplifiedPoints.size(), 4);
        assertTrue(simplifiedPoints.get(0).isSame(topLeft));
        assertTrue(simplifiedPoints.get(1).isSame(topRight));
        assertTrue(simplifiedPoints.get(2).isSame(bottomRight));
        assertTrue(simplifiedPoints.get(3).isSame(bottomLeft));
    }

    @Test
    void simplifySummits_withTwoBackToBackRedundants() {
        // arrange
        Point topLeft = new Point(0, 10);
        Point topRight = new Point(10, 10);
        Point bottomRight = new Point(10, 0);
        Point bottomLeft = new Point(0, 0);

        Point redundant1 = new Point(0, 3);
        Point redundant2 = new Point(0, 7);

        List<Point> summits = new ArrayList<>();
        summits.add(redundant2);
        summits.add(topLeft);
        summits.add(topRight);
        summits.add(bottomRight);
        summits.add(bottomLeft);
        summits.add(redundant1);

        AbstractShape shape = new AbstractShape(summits);

        // act
        List<Point> simplifiedPoints = ShapeHelper.simplifySummits(shape);

        // assert
        assertEquals(simplifiedPoints.size(), 4);
        assertTrue(simplifiedPoints.get(0).isSame(topLeft));
        assertTrue(simplifiedPoints.get(1).isSame(topRight));
        assertTrue(simplifiedPoints.get(2).isSame(bottomRight));
        assertTrue(simplifiedPoints.get(3).isSame(bottomLeft));
    }
}