package utils;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PointTest {

    @Test
    public void isInside_ifInside_shouldReturnTrue() {
        // arrange
        List<Point> summits = new ArrayList<>();
        summits.add(new Point(0, 0));
        summits.add(new Point(-10, -5));
        summits.add(new Point(-5, -10));
        summits.add(new Point(5, -10));
        summits.add(new Point(10, -5));

        // act
        boolean isInside = new Point(0, -3).isInside(summits, false);

        // assert
        assertTrue(isInside);
    }

    @Test
    public void isInside_ifNotInside_shouldReturnFalse() {
        // arrange
        List<Point> summits = new ArrayList<>();
        summits.add(new Point(0, 0));
        summits.add(new Point(-10, -5));
        summits.add(new Point(-5, -10));
        summits.add(new Point(5, -10));
        summits.add(new Point(10, -5));

        // act
        boolean isInside = new Point(0, 3).isInside(summits, true);

        // assert
        assertFalse(isInside);
    }

    @Test
    public void isInside_ifOnEdge_shouldReturnTrue() {
        // arrange
        List<Point> summits = new ArrayList<>();
        summits.add(new Point(0, 0));
        summits.add(new Point(-10, -5));
        summits.add(new Point(-5, -10));
        summits.add(new Point(5, -10));
        summits.add(new Point(10, -5));

        // act
        boolean isInside = new Point(-6, -3).isInside(summits, true);

        // assert
        assertTrue(isInside);
    }

    @Test
    public void isInside_ifOnSummit_shouldReturnTrue() {
        // arrange
        List<Point> summits = new ArrayList<>();
        summits.add(new Point(0, 0));
        summits.add(new Point(-10, -5));
        summits.add(new Point(-5, -10));
        summits.add(new Point(5, -10));
        summits.add(new Point(10, -5));

        // act
        boolean isInside = new Point(0, 0).isInside(summits, true);

        // assert
        assertTrue(isInside);
    }

    @Test
    public void weirdCase() {
        // arrange
        List<Point> summits = new ArrayList<>();
        summits.add(new Point(1.20, 1.04));
        summits.add(new Point(1.20, 3.22));
        summits.add(new Point(2.34, 3.22));
        summits.add(new Point(2.34, 4.30));
        summits.add(new Point(3.58, 4.30));
        summits.add(new Point(3.58, 2.23));
        summits.add(new Point(2.95, 2.23));
        summits.add(new Point(2.95, 1.04));

        // act
        boolean isInside = new Point(1.4, 1.35).isInside(summits, false);

        // assert
        assertTrue(isInside);
    }

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

        // act
        List<Point> simplifiedPoints = Point.removeRedundantSummits(summits);

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

        // act
        List<Point> simplifiedPoints = Point.removeRedundantSummits(summits);

        // assert
        assertEquals(simplifiedPoints.size(), 4);
        assertTrue(simplifiedPoints.get(0).isSame(topLeft));
        assertTrue(simplifiedPoints.get(1).isSame(topRight));
        assertTrue(simplifiedPoints.get(2).isSame(bottomRight));
        assertTrue(simplifiedPoints.get(3).isSame(bottomLeft));
    }
}