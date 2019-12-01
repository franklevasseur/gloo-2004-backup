package Domain;

import org.junit.jupiter.api.Test;
import utils.Color;
import utils.Point;
import utils.Segment;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TileTest {

    @Test
    void cutSideToSide() {
        List<Point> summits = new ArrayList<>();
        summits.add(new Point(3.90,2.77));
        summits.add(new Point(3.90,2.84));
        summits.add(new Point(3.70,2.84));
        summits.add(new Point(3.70,2.54));
        Tile tile = new Tile(summits, new Material(Color.RED, MaterialType.tileMaterial, "test"));

        Segment cuttingSegment = new Segment(new Point(3.82, 0.94), new Point(3.82, 2.77));

        // act
        List<Tile> resultantTiles = tile.cutSideToSide(cuttingSegment, true);

        // assert
        Tile firstHalf = resultantTiles.get(0);
        Tile secondHalf = resultantTiles.get(1);

        assertEquals(firstHalf.getSummits().size(), 4);
        assertEquals(secondHalf.getSummits().size(), 4);
    }

    @Test
    void cutSideToSideWithAngle() {
        Point topLeft = new Point(0, 10);
        Point topRight = new Point(10, 10);
        Point bottomRight = new Point(10, 0);
        Point bottomLeft = new Point(0, 0);

        List<Point> summits = new ArrayList<>();
        summits.add(topLeft);
        summits.add(topRight);
        summits.add(bottomRight);
        summits.add(bottomLeft);

        Tile tile = new Tile(summits, new Material(Color.RED, MaterialType.tileMaterial, "test"));

        Point intersectBottomRight = new Point(10, 3);
        Point intersectTopLeft = new Point(0, 7);
        Segment desiredCut = new Segment(intersectTopLeft, intersectBottomRight);

        double pt1Y = desiredCut.predictY(-2);
        double pt2Y = desiredCut.predictY(12);

        Segment cuttingSegment = new Segment(new Point(-2, pt1Y), new Point(12, pt2Y));

        // act
        List<Tile> resultantTiles = tile.cutSideToSide(cuttingSegment, false);

        // assert
        Tile firstHalf = resultantTiles.get(0);
        Tile secondHalf = resultantTiles.get(1);

        assertEquals(firstHalf.getSummits().size(), 4);
        assertTrue(firstHalf.getSummits().get(0).isSame(topLeft));
        assertTrue(firstHalf.getSummits().get(1).isSame(topRight));
        assertTrue(firstHalf.getSummits().get(2).isSame(intersectBottomRight));
        assertTrue(firstHalf.getSummits().get(3).isSame(intersectTopLeft));

        assertEquals(secondHalf.getSummits().size(), 4);
        assertTrue(secondHalf.getSummits().get(0).isSame(intersectBottomRight));
        assertTrue(secondHalf.getSummits().get(1).isSame(bottomRight));
        assertTrue(secondHalf.getSummits().get(2).isSame(bottomLeft));
        assertTrue(secondHalf.getSummits().get(3).isSame(intersectTopLeft));
    }
}