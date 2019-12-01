package Domain;

import utils.*;

import java.util.ArrayList;
import java.util.List;

class SurfaceFiller {

    private TileCutter tileCutter = new TileCutter();

    public List<Tile> fillSurface(Surface surface, Tile masterTile, SealsInfo sealsInfo, PatternType type) {

        if (type == PatternType.DEFAULT) {
            return fillSurfaceWithDefaults(surface, masterTile, sealsInfo);
        }

        if (type == PatternType.HORIZONTAL_SHIFT) {
            return fillSurfaceWithHorizontalShift(surface, masterTile, sealsInfo);
        }

        if (type == PatternType.VERTICAL_SHIFT) {
            return fillSurfaceWithVertcialShift(surface, masterTile, sealsInfo);
        }

        return fillSurfaceWithDefaults(surface, masterTile, sealsInfo);
    }

    private List<Tile> fillSurfaceWithDefaults(Surface surface, Tile masterTile, SealsInfo sealing) {
        return fillSurface(surface, masterTile, sealing, 0, 0);
    }

    private List<Tile> fillSurfaceWithHorizontalShift(Surface surface, Tile masterTile, SealsInfo sealing) {
        double shift = masterTile.getWidth() / 2; // TODO make this a parameter...
        return fillSurface(surface, masterTile, sealing, shift, 0);
    }

    private List<Tile> fillSurfaceWithVertcialShift(Surface surface, Tile masterTile, SealsInfo sealing) {
        double shift = masterTile.getWidth() / 2; // TODO make this a parameter...
        return fillSurface(surface, masterTile, sealing, 0, shift);
    }


    private List<Tile> fillSurface(Surface surface, Tile masterTile, SealsInfo sealing, double horizontalShift, double verticalShift) {


        AbstractShape surfaceShape = new AbstractShape(surface.getSummits(), false);
        utils.Point surfaceTopLeftCorner = ShapeHelper.getTheoricalTopLeftCorner(surfaceShape);
        double surfaceWidth = ShapeHelper.getWidth(surfaceShape);
        double surfaceHeight = ShapeHelper.getHeight(surfaceShape);

        RectangleInfo info = RectangleHelper.summitsToRectangleInfo(masterTile.getSummits());
        double tileWidth = info.width;
        double tileHeight = info.height;

        double horizontalBaseShift = horizontalShift % tileWidth;
        double actualHorizontalShift = horizontalBaseShift == 0 ? 0 : horizontalBaseShift - tileWidth;

        double verticalBaseShift = verticalShift % tileHeight;
        double actualVerticalShift = verticalBaseShift == 0 ? 0 : verticalBaseShift - tileHeight;

        List<Tile> tiles = new ArrayList<>();

        double unitOfWidth = tileWidth + sealing.getWidth();
        double unitOfHeight = tileHeight + sealing.getWidth();

        int amountOfLines = (int) Math.ceil(surfaceHeight / unitOfHeight) + 2; // 2 is for security...
        int amountOfColumns = (int) Math.ceil(surfaceWidth / unitOfWidth) + 2;

        Point masterTileRelativeCorner = info.topLeftCorner;
        Point masterTileAbsoluteCorner = Point.translate(surfaceTopLeftCorner, masterTileRelativeCorner.x, masterTileRelativeCorner.y);

        int masterTileColumnIndex = (int) Math.ceil(masterTileRelativeCorner.x / tileWidth);
        int masterTileLineIndex = (int) Math.ceil(masterTileRelativeCorner.y / tileHeight);

        double xTranslation = -(masterTileColumnIndex * tileWidth);
        double yTranslation = -(masterTileLineIndex * tileHeight);

        Point firstCorner = Point.translate(masterTileAbsoluteCorner, xTranslation, yTranslation);

        for (int line = 0; line < amountOfLines; line++) {
            for (int column = 0; column < amountOfColumns; column++) {
                Point topLeftCorner = Point.translate(firstCorner, column * unitOfWidth, line * unitOfHeight);

                if (line % 2 == 1) {
                    topLeftCorner = topLeftCorner.translate(new Point(actualHorizontalShift, 0));
                }
                if (column % 2 == 1) {
                    topLeftCorner = topLeftCorner.translate(new Point(0, actualVerticalShift));
                }

                Tile nextTile = new Tile(RectangleHelper.rectangleInfoToSummits(topLeftCorner, tileWidth, tileHeight), masterTile.getMaterial());

                tiles.add(nextTile);
            }
        }

        return tileCutter.cutTilesThatExceed(surface, tiles);
    }
}
