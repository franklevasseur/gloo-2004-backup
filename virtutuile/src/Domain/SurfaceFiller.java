package Domain;

import application.TileDto;
import utils.AbstractShape;
import utils.Point;
import utils.RectangleHelper;
import utils.RectangleInfo;
import utils.ShapeHelper;

import java.util.ArrayList;
import java.util.List;

class SurfaceFiller {

    public static List<Tile> fillSurface(Surface surface, Tile masterTile, SealsInfo sealsInfo, PatternType type) {
        return fillSurfaceWithDefaults(surface, masterTile, sealsInfo);
    }

    private static List<Tile> fillSurfaceWithDefaults(Surface surface, Tile masterTile, SealsInfo pSealsInfo) {

//        AbstractShape surfaceShape = new AbstractShape(surfaceToFillDto.summits, false);
//        utils.Point surfaceTopLeftCorner = ShapeHelper.getTopLeftCorner(surfaceShape);
//        double surfaceWidth = ShapeHelper.getWidth(surfaceShape);
//        double surfaceHeight = ShapeHelper.getHeight(surfaceShape);
//
//        RectangleInfo info = RectangleHelper.summitsToRectangleInfo(masterTile.summits);
//        double tileWidth = info.width;
//        double tileHeight = info.height;
//
//        List<TileDto> tiles = new ArrayList<>();
//
//        double unitOfWidth = tileWidth + sealing.sealWidth;
//        double unitOfHeight = tileHeight + sealing.sealWidth;
//
//        int amountOfLines = (int) Math.ceil(surfaceHeight / unitOfHeight);
//        int amountOfColumns = (int) Math.ceil(surfaceWidth / unitOfWidth);
//
//        for (int line = 0; line < amountOfLines; line++) {
//            for (int column = 0; column < amountOfColumns; column++) {
//                TileDto nextTile = new TileDto();
//
//
//                utils.Point topLeftCorner = Point.translate(surfaceTopLeftCorner, column * unitOfWidth, line * unitOfHeight);
//
//                double rightSurfaceBound = surfaceTopLeftCorner.x + surfaceWidth;
//                double bottomSurfaceBound = surfaceTopLeftCorner.y + surfaceHeight;
//
//                boolean isTileOverflowX = topLeftCorner.x + tileWidth > rightSurfaceBound;
//                boolean isTileOverflowY = topLeftCorner.y + tileHeight > bottomSurfaceBound;
//                double actualWidth = isTileOverflowX ? rightSurfaceBound - topLeftCorner.x : tileWidth;
//                double actualHeight = isTileOverflowY ? bottomSurfaceBound - topLeftCorner.y : tileHeight;
//
//                nextTile.summits = RectangleHelper.rectangleInfoToSummits(topLeftCorner, actualWidth, actualHeight);
//                nextTile.material = masterTile.material;
//
//                tiles.add(nextTile);
//            }
//        }
//
//        surfaceToFillDto.tiles = tiles;
//        surfaceToFillDto.isHole = HoleStatus.FILLED;
//        surfaceToFillDto.sealsInfoDto = sealing;
//
//        return tiles;

        return new ArrayList<>();
    }
}
