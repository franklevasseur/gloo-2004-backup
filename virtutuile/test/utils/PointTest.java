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
}