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
        boolean isInside = new Point(0, -3).isInside(summits);

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
        boolean isInside = new Point(0, 3).isInside(summits);

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
        boolean isInside = new Point(-6, -3).isInside(summits);

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
        boolean isInside = new Point(0, 0).isInside(summits);

        // assert
        assertTrue(isInside);
    }
}