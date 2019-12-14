package Domain;

import utils.*;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TileCutter implements Serializable {

    public List<Tile> cutTilesThatExceed(Surface surface, List<Tile> tiles) {
        List<Tile> insideTiles = tiles.stream().filter(t -> !isAllOutside(surface, t, true)).collect(Collectors.toList());
        List<Tile> tilesToCut = insideTiles.stream().filter(t -> !isAllInside(surface, t)).collect(Collectors.toList());
        insideTiles.removeIf(tilesToCut::contains);

//        uncomment to display in red desired tiles...
//        Material newMaterialToCut = new Material(Color.RED, MaterialType.tileMaterial, "to cut");
//        tilesToCut.forEach(t -> t.setMaterial(newMaterialToCut));

        List<Tile> tileResultantOfCut = cutTilesThatNeedToBeCut(surface, tilesToCut);
        List<Tile> newKeepers = tileResultantOfCut.stream().filter(t ->
                !isAllOutside(surface, t, false)).collect(Collectors.toList());
        insideTiles.addAll(newKeepers);

        return insideTiles;
    }

    public boolean isAllInside(Surface surface, Tile tile) {
        if (surface.isFusionned()) {
            FusionnedSurface fs = (FusionnedSurface) surface;

            return fs.getFusionnedSurfaces().stream().allMatch(s -> {
                if (s.isHole() == HoleStatus.HOLE) {
                    return isAllOutside(s, tile, true);
                }
                return isAllInside(s, tile);
            });
        }

        AbstractShape surfaceShape = new AbstractShape(surface.getSummits());
        AbstractShape tileShape = new AbstractShape(tile.getSummits());
        return ShapeHelper.isAllInside(tileShape, surfaceShape);
    }

    public boolean isAllOutside(Surface surface, Tile tile, boolean considerIntersections) {
        if (surface.isFusionned()) {
            FusionnedSurface fs = (FusionnedSurface) surface;

            boolean accumulator = true;
            for (Surface s : fs.getFusionnedSurfaces()) {
                if (s.isHole() == HoleStatus.HOLE) {
                    if (isAllInside(s, tile)) {
                        return true;
                    }
                } else {
                    if (!isAllOutside(s, tile, considerIntersections)) {
                        accumulator = false; // the only way it can still be outside here is if its all inside a hole that still has not been seen in the loop
                    }
                }
            }

            return accumulator;
        }

        AbstractShape surfaceShape = new AbstractShape(surface.getSummits());
        return isAllOutsidePolygon(surfaceShape, tile, considerIntersections);
    }

    public boolean isAllOutsidePolygon(AbstractShape surfaceShape, Tile tile, boolean considerIntersections) {
        AbstractShape tileShape = new AbstractShape(tile.getSummits());

        if (considerIntersections) {
            for (Segment segment : Segment.fromPoints(surfaceShape.summits)) {
                List<Point> intersection = tile.findIntersections(segment, false);
                List<Point> forbiddenPoints = Arrays.asList(surfaceShape.summits, tile.getSummits()).stream().flatMap(x -> x.stream()).collect(Collectors.toList());
                intersection.removeIf(i -> forbiddenPoints.stream().anyMatch(p -> p.isInRange(i, Point.DOUBLE_TOLERANCE)));
                if (intersection.size() > 0) {
                    return false;
                }
            }
        }

        return ShapeHelper.isAllOutside(tileShape, surfaceShape);
    }

    private List<Tile> cutTilesThatNeedToBeCut(Surface surface, List<Tile> tiles) {
        if (!isOnlyOnePolygon(surface)) {
//            System.out.println("Detected a Surface that is more than one Polygon or contains an inner hole. These surfaces are hard to handle mon p'tit chummé :P...");
            FusionnedSurface fs = (FusionnedSurface) surface;

            List<Surface> noHoles = fs.getFusionnedSurfaces().stream().filter(s -> s.isHole() != HoleStatus.HOLE).collect(Collectors.toList());
            List<Surface> holes = fs.getFusionnedSurfaces().stream().filter(s -> s.isHole() == HoleStatus.HOLE).collect(Collectors.toList());

            List<AbstractShape> allPlainPolygons = FusionHelper.getFusionResultSummits(noHoles.stream().map(s -> new AbstractShape(s.getSummits())).collect(Collectors.toList()));
            List<AbstractShape> allHolesPolygons = holes.stream().map(h -> new AbstractShape(h.getSummits(), true)).collect(Collectors.toList());

            List<Tile> allCuts = tiles;
            for (AbstractShape plainPolygon : allPlainPolygons) {
                allCuts = allCuts.stream().flatMap(t -> cutOneTile(plainPolygon, t).stream()).collect(Collectors.toList());
            }

            for (AbstractShape holePolygon : allHolesPolygons) {
                allCuts = allCuts.stream().flatMap(t -> cutOneTile(holePolygon, t).stream()).collect(Collectors.toList());
            }

            return allCuts;
        }

        return tiles.stream().flatMap(t -> cutOneTile(surface, t).stream()).collect(Collectors.toList());
    }

    private boolean isOnlyOnePolygon(Surface surface) {
        if (!surface.isFusionned()) {
            return true;
        }

        FusionnedSurface fs = (FusionnedSurface) surface;
        List<AbstractShape> abstractShapes = fs.getFusionnedSurfaces().stream().map(s -> new AbstractShape(s.getSummits(), s.isHole() == HoleStatus.HOLE)).collect(Collectors.toList());
        List<AbstractShape> allPlainPolygons = FusionHelper.getFusionResultSummits(abstractShapes);
        return allPlainPolygons.size() == 1;
    }

    private List<Tile> cutOneTile(Surface surface, Tile tileToCut) {
        return cutOneTile(new AbstractShape(surface.getSummits(), surface.isHole() == HoleStatus.HOLE), tileToCut);
    }

    private List<Tile> cutOneTile(AbstractShape surfaceShape, Tile tileToCut) {

        List<Segment> surfaceSegments = Segment.fromPoints(surfaceShape.summits);
        List<Tile> returned = Arrays.asList(tileToCut);

        for (Segment seg : surfaceSegments) {
            returned = returned.stream()
                    .flatMap(t -> t.doOneCut(seg).stream())
//                    .filter(t -> !this.isAllOutsidePolygon(surfaceShape, tileToCut, false))
                    .collect(Collectors.toList());
        }

        return returned;
    }
}
