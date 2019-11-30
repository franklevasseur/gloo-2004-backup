package Domain;

import utils.*;

import java.util.ArrayList;
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
                Tile nextTile = new Tile();

                utils.Point topLeftCorner = Point.translate(surfaceTopLeftCorner, column * unitOfWidth, line * unitOfHeight);

                double rightSurfaceBound = surfaceTopLeftCorner.x + surfaceWidth;
                double bottomSurfaceBound = surfaceTopLeftCorner.y + surfaceHeight;

                boolean isTileOverflowX = topLeftCorner.x + tileWidth > rightSurfaceBound;
                boolean isTileOverflowY = topLeftCorner.y + tileHeight > bottomSurfaceBound;
                double actualWidth = isTileOverflowX ? rightSurfaceBound - topLeftCorner.x : tileWidth;
                double actualHeight = isTileOverflowY ? bottomSurfaceBound - topLeftCorner.y : tileHeight;

                nextTile.setSummits(RectangleHelper.rectangleInfoToSummits(topLeftCorner, actualWidth, actualHeight));
                nextTile.setMaterial(masterTile.getMaterial());

                tiles.add(nextTile);
            }
        }

        return cutTilesThatExceed(surface, tiles);
    }

    private static List<Tile> cutTilesThatExceed(Surface surface, List<Tile> tiles) {
        List<Tile> insideTiles = tiles.stream().filter(t -> !isAllOutside(surface, t)).collect(Collectors.toList());

        List<Tile> tilesToCut = insideTiles.stream().filter(t -> !isAllInside(surface, t)).collect(Collectors.toList());

        Material newMaterialToCut = new Material(Color.RED, MaterialType.tileMaterial, "to cut");
        tilesToCut.forEach(t -> t.setMaterial(newMaterialToCut));

        return  insideTiles;
    }

    private static boolean isAllInside(Surface surface, Tile tile) {
        return tile.getSummits().stream().allMatch(s -> s.isInside(surface.getSummits()));
    }

    private static boolean isAllOutside(Surface surface, Tile tile) {
        return tile.getSummits().stream().allMatch(s -> !s.isInside(surface.getSummits()));
    }
}
