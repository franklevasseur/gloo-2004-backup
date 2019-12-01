package Domain;

import utils.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

class SurfaceFiller {

    public static List<Tile> fillSurface(Surface surface, Tile masterTile, SealsInfo sealsInfo, PatternType type) {
        return fillSurfaceWithDefaults(surface, masterTile, sealsInfo);
    }

    private static List<Tile> fillSurfaceWithDefaults(Surface surface, Tile masterTile, SealsInfo sealing) {

        AbstractShape surfaceShape = new AbstractShape(surface.getSummits(), false);
        utils.Point surfaceTopLeftCorner = ShapeHelper.getTheoricalTopLeftCorner(surfaceShape);
        double surfaceWidth = ShapeHelper.getWidth(surfaceShape);
        double surfaceHeight = ShapeHelper.getHeight(surfaceShape);

        RectangleInfo info = RectangleHelper.summitsToRectangleInfo(masterTile.getSummits());
        double tileWidth = info.width;
        double tileHeight = info.height;

        List<Tile> tiles = new ArrayList<>();

        double unitOfWidth = tileWidth + sealing.getWidth();
        double unitOfHeight = tileHeight + sealing.getWidth();

        int amountOfLines = (int) Math.ceil(surfaceHeight / unitOfHeight);
        int amountOfColumns = (int) Math.ceil(surfaceWidth / unitOfWidth);

        for (int line = 0; line < amountOfLines; line++) {
            for (int column = 0; column < amountOfColumns; column++) {
                utils.Point topLeftCorner = Point.translate(surfaceTopLeftCorner, column * unitOfWidth, line * unitOfHeight);

                double rightSurfaceBound = surfaceTopLeftCorner.x + surfaceWidth;
                double bottomSurfaceBound = surfaceTopLeftCorner.y + surfaceHeight;

                boolean isTileOverflowX = topLeftCorner.x + tileWidth > rightSurfaceBound;
                boolean isTileOverflowY = topLeftCorner.y + tileHeight > bottomSurfaceBound;
                double actualWidth = isTileOverflowX ? rightSurfaceBound - topLeftCorner.x : tileWidth;
                double actualHeight = isTileOverflowY ? bottomSurfaceBound - topLeftCorner.y : tileHeight;

                Tile nextTile = new Tile(RectangleHelper.rectangleInfoToSummits(topLeftCorner, actualWidth, actualHeight), masterTile.getMaterial());

                tiles.add(nextTile);
            }
        }

        return cutTilesThatExceed(surface, tiles);
    }

    private static List<Tile> cutTilesThatExceed(Surface surface, List<Tile> tiles) {
        List<Tile> insideTiles = tiles.stream().filter(t -> !isAllOutside(surface, t)).collect(Collectors.toList());
        List<Tile> tilesToCut = insideTiles.stream().filter(t -> !isAllInside(surface, t)).collect(Collectors.toList());
        insideTiles.removeIf(tilesToCut::contains);

//        uncomment to display in red desired tiles...
//        Material newMaterialToCut = new Material(Color.RED, MaterialType.tileMaterial, "to cut");
//        tilesToCut.forEach(t -> t.setMaterial(newMaterialToCut));

        List<Tile> tileResultantOfCut = cutTilesThatNeedToBeCut(surface, tilesToCut);
        List<Tile> newKeepers = tileResultantOfCut.stream().filter(t -> !isAllOutside(surface, t)).collect(Collectors.toList());

        insideTiles.addAll(newKeepers);
        return insideTiles;
    }

    private static boolean isAllInside(Surface surface, Tile tile) {
        AbstractShape surfaceShape = new AbstractShape(surface.getSummits());
        AbstractShape tileShape = new AbstractShape(tile.getSummits());
        return ShapeHelper.isAllInside(tileShape, surfaceShape);
    }

    private static boolean isAllOutside(Surface surface, Tile tile) {
        AbstractShape surfaceShape = new AbstractShape(surface.getSummits());
        AbstractShape tileShape = new AbstractShape(tile.getSummits());
        return ShapeHelper.isAllOutside(tileShape, surfaceShape);
    }

    private static List<Tile> cutTilesThatNeedToBeCut(Surface surface, List<Tile> tiles) {
        List<Segment> surfaceSegments = Segment.toSegments(surface.getSummits());
        return tiles.stream().flatMap(t -> cutOneTile(surfaceSegments, t).stream()).collect(Collectors.toList());
    }

    private static List<Tile> cutOneTile(List<Segment> surfaceSegments, Tile tileToCut) {
        List<Segment> tileSegments = Segment.toSegments(tileToCut.getSummits());

        List<Segment> segmentsThatPartiallyCutTheTile = new ArrayList<>();
        for (Segment seg: surfaceSegments) {
            int nInterSections = Segment.findIntersection(seg, tileSegments).size();
            if (nInterSections == 2) {
                return tileToCut.cutSideToSide(seg);
            } else if (nInterSections == 1 || seg.isInside(tileSegments, false)) {
                segmentsThatPartiallyCutTheTile.add(seg);
            }
        }

        // TODO: attention il y a peut-être deux différents boutes de segments qui rentrent dans la tuile... ici on suppose qu'il n'y a qu'un pick
        if (segmentsThatPartiallyCutTheTile.size() >= 2) {
            return tileToCut.cut(segmentsThatPartiallyCutTheTile);
        }
        return Arrays.asList(tileToCut);
    }
}
