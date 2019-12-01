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
}