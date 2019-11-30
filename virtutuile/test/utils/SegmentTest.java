package utils;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SegmentTest {

    @Test
    public void toSegments_shouldWorkLol() {

        // arrange
        List<Point> summits = new ArrayList<>();
        summits.add(new Point(0, 0));
        summits.add(new Point(-10, -5));
        summits.add(new Point(-5, -10));
        summits.add(new Point(5, -10));
        summits.add(new Point(10, -5));

        // act
        List<Segment> segments = Segment.toSegments(summits);

        // assert
        List<Segment> expectedSegments = new ArrayList<>();
        expectedSegments.add(new Segment(summits.get(0), summits.get(1)));
        expectedSegments.add(new Segment(summits.get(1), summits.get(2)));
        expectedSegments.add(new Segment(summits.get(2), summits.get(3)));
        expectedSegments.add(new Segment(summits.get(3), summits.get(4)));
        expectedSegments.add(new Segment(summits.get(4), summits.get(0)));

        assertEquals(segments.size(), 5);
        assertTrue(segments.get(0).isSame(expectedSegments.get(0)));
        assertTrue(segments.get(1).isSame(expectedSegments.get(1)));
        assertTrue(segments.get(2).isSame(expectedSegments.get(2)));
        assertTrue(segments.get(3).isSame(expectedSegments.get(3)));
        assertTrue(segments.get(4).isSame(expectedSegments.get(4)));
    }

    @Test
    public void intersect_shouldWork() {
        // arrange
        Segment seg1 = new Segment(new Point(0, 0), new Point(10, 10));
        Segment seg2 = new Segment(new Point(0, 10), new Point(10, 0));
        Segment seg3 = new Segment(new Point(-10, 0), new Point(0, 10));

        // act
        Point intersection1 = seg1.intersect(seg2); // 5, 5
        Point intersection2 = seg1.intersect(seg3); // null
        Point intersection3 = seg2.intersect(seg3); // 0, 10

        // assert
        assertTrue(intersection1.isSame(new Point(5, 5)));
        assertNull(intersection2);
        assertTrue(intersection3.isSame(new Point(0, 10)));
    }

    @Test
    public void intersect_withInfiniteSegments_shouldStillWork() {
        // arrange
        Segment infiniteSegment = new Segment(new Point(0, -3), new Point(Double.MAX_VALUE, -3));

        Segment seg1 = new Segment(new Point(0, 0), new Point(-10, -5));
        Segment seg2 = new Segment(new Point(10, -5), new Point(0,0));

        // act
        boolean doesIntersect1 = seg1.doesIntersect(infiniteSegment);
        boolean doesIntersect2 = seg2.doesIntersect(infiniteSegment);

        // assert
        assertTrue(doesIntersect2);
        assertFalse(doesIntersect1);
    }

    @Test
    public void intersect_withFirstSummit_shoulReturnTrue() {
        // arrange
        Segment seg1 = new Segment(new Point(0, 0), new Point(-10, -10));
        Segment seg2 = new Segment(new Point(0, 0), new Point(10, 5));

        // act
        Point intersect = seg1.intersect(seg2);

        // assert
        assertTrue(intersect.isSame(new Point(0, 0)));
    }

    @Test
    public void countIntersectionsWithTile() {
        // arrange
        List<Point> tileSummits = new ArrayList<>();
        tileSummits.add(new Point(0, 0));
        tileSummits.add(new Point(0, 10));
        tileSummits.add(new Point(10, 10));
        tileSummits.add(new Point(10, 0));
        List<Segment> tileSegments = Segment.toSegments(tileSummits);

        Segment cuttingSegment = new Segment(new Point(-5, 5), new Point(15, 5));

        // act
        List<Point> intersections = Segment.findIntersection(cuttingSegment, tileSegments);

        // assert
        assertEquals(intersections.size(), 2);
        assertTrue(intersections.get(0).isSame(new Point(0, 5)));
        assertTrue(intersections.get(1).isSame(new Point(10, 5)));
    }
}