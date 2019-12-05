package Domain;

import utils.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

class SurfaceFiller implements Serializable {

    private TileCutter tileCutter = new TileCutter();

    public List<Tile> fillSurface(Surface surface, Tile masterTile, SealsInfo sealsInfo, PatternType type, double angle, double shifting) {

        if (type == PatternType.DEFAULT) {
            return fillSurfaceWithDefaults(surface, masterTile, sealsInfo, angle);
        }

        if (type == PatternType.HORIZONTAL_SHIFT) {
            return fillSurfaceWithOneOrientation(surface, masterTile, sealsInfo, shifting, 0, angle);
        }

        if (type == PatternType.VERTICAL_SHIFT) {
            return fillSurfaceWithOneOrientation(surface, masterTile, sealsInfo, 0, shifting, angle);
        }

        if (type == PatternType.MIX) {
            return fillSurfaceWithMix(surface, masterTile, sealsInfo);
        }

        if (type == PatternType.GROUP_MIX) {
            return fillSurfaceWithGroupMix(surface, masterTile, sealsInfo);
        }

        return fillSurfaceWithDefaults(surface, masterTile, sealsInfo, 0);
    }

    private List<Tile> fillSurfaceWithDefaults(Surface surface, Tile masterTile, SealsInfo sealing, double angle) {
        return fillSurfaceWithOneOrientation(surface, masterTile, sealing, 0, 0, angle);
    }

    private  List<Tile> fillSurfaceWithMix(Surface surface, Tile pMasterTile, SealsInfo sealing) {
        AbstractShape surfaceShape = new AbstractShape(surface.getSummits(), false);
        utils.Point surfaceTopLeftCorner = ShapeHelper.getTheoricalTopLeftCorner(surfaceShape);
        double surfaceWidth = ShapeHelper.getWidth(surfaceShape);
        double surfaceHeight = ShapeHelper.getHeight(surfaceShape);

        Tile masterTile = pMasterTile.deepCopy();
        if (RectangleHelper.getOrientation(masterTile.getSummits()) == RectangleOrientation.HORIZONTAL) {
            masterTile.setSummits(RectangleHelper.flip(masterTile.getSummits()));
        }

        RectangleInfo info = RectangleHelper.summitsToRectangleInfo(masterTile.getSummits());
        double tileWidth = info.width;
        double tileHeight = info.height;

        List<Tile> tiles = new ArrayList<>();

        double unitOfWidth = (2 * tileWidth) + tileHeight + (4 * sealing.getWidth());
        double unitOfHeight = tileWidth + sealing.getWidth();

        int amountOfLines = (int) Math.ceil(surfaceHeight / unitOfHeight) + 2; // 2 is for security...
        int amountOfColumns = (int) Math.ceil(surfaceWidth / unitOfWidth) + 2;

        Point masterTileRelativeCorner = info.topLeftCorner;
        Point masterTileAbsoluteCorner = Point.translate(surfaceTopLeftCorner, masterTileRelativeCorner.x, masterTileRelativeCorner.y);

        int masterTileColumnIndex = (int) Math.ceil(masterTileRelativeCorner.x / tileWidth);
        int masterTileLineIndex = (int) Math.ceil(masterTileRelativeCorner.y / tileHeight);

        double xTranslation = -(masterTileColumnIndex * tileWidth);
        double yTranslation = -(masterTileLineIndex * tileHeight);

        Point firstCorner = Point.translate(masterTileAbsoluteCorner, xTranslation, yTranslation);

        for (int line = -1; line < amountOfLines; line++) {
            for (int column = 0; column < amountOfColumns; column++) {
                Point verticalTileTopLeft = Point.translate(firstCorner, column * unitOfWidth, line * unitOfHeight);
                Point horizontalTileTopLeft = verticalTileTopLeft.translate(new Point(tileWidth + sealing.getWidth(), (tileHeight - tileWidth + sealing.getWidth())));

                double horizontalShift = tileWidth + sealing.getWidth();
                int nShift = line % 4;
                verticalTileTopLeft = verticalTileTopLeft.translate(new Point(-nShift * horizontalShift, 0));
                horizontalTileTopLeft = horizontalTileTopLeft.translate(new Point(-nShift * horizontalShift, 0));

                Tile horizontalTile = new Tile(RectangleHelper.rectangleInfoToSummits(verticalTileTopLeft, tileWidth, tileHeight), masterTile.getMaterial());
                Tile verticalTile = new Tile(RectangleHelper.rectangleInfoToSummits(horizontalTileTopLeft, (tileHeight + sealing.getWidth()), (tileWidth - sealing.getWidth())), masterTile.getMaterial());

                tiles.add(horizontalTile);
                tiles.add(verticalTile);
            }
        }

        return tileCutter.cutTilesThatExceed(surface, tiles);
    }

    private List<Tile> fillSurfaceWithGroupMix(Surface surface, Tile pMasterTile, SealsInfo sealing) {
        AbstractShape surfaceShape = new AbstractShape(surface.getSummits(), false);
        utils.Point surfaceTopLeftCorner = ShapeHelper.getTheoricalTopLeftCorner(surfaceShape);
        double surfaceWidth = ShapeHelper.getWidth(surfaceShape);
        double surfaceHeight = ShapeHelper.getHeight(surfaceShape);

        Tile masterTile = pMasterTile.deepCopy();
        if (RectangleHelper.getOrientation(masterTile.getSummits()) == RectangleOrientation.HORIZONTAL) {
            masterTile.setSummits(RectangleHelper.flip(masterTile.getSummits()));
        }

        RectangleInfo info = RectangleHelper.summitsToRectangleInfo(masterTile.getSummits());
        double tileWidth = info.width;
        double tileHeight = info.height;

        List<Tile> tiles = new ArrayList<>();

        double unitOfWidth = 2 * (tileWidth + sealing.getWidth());
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

        for (int line = -1; line < amountOfLines; line++) {
            for (int column = 0; column < amountOfColumns; column++) {

                Point firstTileTopLeft = Point.translate(firstCorner, column * unitOfWidth, line * unitOfHeight);
                List<Tile> twoTilesToAdd = new ArrayList<>(2);
                if (column % 2 == 1) {
                    Point secondTileTopLeft = firstTileTopLeft.translate(new Point(tileWidth + sealing.getWidth(), 0));

                    List<Point> firstTileSummits = RectangleHelper.rectangleInfoToSummits(firstTileTopLeft, tileWidth, tileHeight);
                    List<Point> secondTileSummits = RectangleHelper.rectangleInfoToSummits(secondTileTopLeft, tileWidth, tileHeight);
                    twoTilesToAdd.add(new Tile(firstTileSummits, masterTile.getMaterial()));
                    twoTilesToAdd.add(new Tile(secondTileSummits, masterTile.getMaterial()));
                } else {
                    double cheatHeight = tileWidth - (sealing.getWidth() / 2);
                    double cheatWidth = tileHeight + sealing.getWidth();

                    Point secondTileTopLeft = firstTileTopLeft.translate(new Point(0, cheatHeight + sealing.getWidth()));

                    List<Point> firstTileSummits = RectangleHelper.rectangleInfoToSummits(firstTileTopLeft, cheatWidth, cheatHeight);
                    List<Point> secondTileSummits = RectangleHelper.rectangleInfoToSummits(secondTileTopLeft, cheatWidth, cheatHeight);
                    twoTilesToAdd.add(new Tile(firstTileSummits, masterTile.getMaterial()));
                    twoTilesToAdd.add(new Tile(secondTileSummits, masterTile.getMaterial()));
                }

                tiles.addAll(twoTilesToAdd);
            }
        }

        return tileCutter.cutTilesThatExceed(surface, tiles);
    }


    private List<Tile> fillSurfaceWithOneOrientation(Surface surface, Tile pMasterTile, SealsInfo sealing, double horizontalShift, double verticalShift, double angle) {

        AbstractShape surfaceShape;
        Tile masterTile = pMasterTile.deepCopy();

        if (angle == 0){
            surfaceShape = new AbstractShape(surface.getSummits());
        } else if (angle == 90) {
            surfaceShape = new AbstractShape(surface.getSummits());
            masterTile.setSummits(RectangleHelper.flip(masterTile.getSummits()));
        } else if (angle > 90 || angle < 0) {
            throw new RuntimeException("L'angle doit être entre 0 et 90 mon ti gars :P");
        }
        else {
            surfaceShape = new AbstractShape(surface.getSummits().stream().map(s -> s.transform(angle)).collect(Collectors.toList()));
        }

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

                RectangleInfo rectangleInfo = new RectangleInfo(topLeftCorner, tileWidth, tileHeight);
                List<Point> summits = RectangleHelper.rectangleInfoToSummits(rectangleInfo);

                if (angle != 0 && angle != 90) {
                    summits = summits.stream().map(x -> x.transformBack(angle)).collect(Collectors.toList());
                }
                Tile nextTile = new Tile(summits, masterTile.getMaterial());

                tiles.add(nextTile);
            }
        }

        return tileCutter.cutTilesThatExceed(surface, tiles);
    }
}
