package Domain;

import utils.AbstractShape;
import utils.FusionHelper;
import utils.Segment;
import utils.ShapeHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TileCutter {

    public List<Tile> cutTilesThatExceed(Surface surface, List<Tile> tiles) {
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

    public boolean isAllInside(Surface surface, Tile tile) {
        if (surface.isFusionned()) {
            FusionnedSurface fs = (FusionnedSurface) surface;

            return fs.getFusionnedSurfaces().stream().allMatch(s -> {
               if (s.isHole() == HoleStatus.HOLE) {
                   return isAllOutside(s, tile);
               }
               return isAllInside(s, tile);
            });
        }

        AbstractShape surfaceShape = new AbstractShape(surface.getSummits());
        AbstractShape tileShape = new AbstractShape(tile.getSummits());
        return ShapeHelper.isAllInside(tileShape, surfaceShape);
    }

    public boolean isAllOutside(Surface surface, Tile tile) {
        if (surface.isFusionned()) {
            FusionnedSurface fs = (FusionnedSurface) surface;

            boolean accumulator = true;
            for (Surface s: fs.getFusionnedSurfaces()) {
                if (s.isHole() == HoleStatus.HOLE) {
                    if (isAllInside(s, tile)) {
                        return true;
                    }
                } else {
                    if (!isAllOutside(s, tile)) {
                        accumulator = false; // the only way it can still be outside here is if its all inside a hole that still has not been seen in the loop
                    }
                }
            }

            return accumulator;
        }

        AbstractShape surfaceShape = new AbstractShape(surface.getSummits());
        AbstractShape tileShape = new AbstractShape(tile.getSummits());
        return ShapeHelper.isAllOutside(tileShape, surfaceShape);
    }

    private List<Tile> cutTilesThatNeedToBeCut(Surface surface, List<Tile> tiles) {
        if (!isOnlyOnePolygon(surface)) {
            System.out.println("Detected a Surface that is more than one Polygon or contains an inner hole. These surfaces are hard to handle...");
            FusionnedSurface fs = (FusionnedSurface) surface;
            List<Surface> noHoles = fs.getFusionnedSurfaces().stream().filter(s -> s.isHole() != HoleStatus.HOLE).collect(Collectors.toList());
            List<Surface> holes = fs.getFusionnedSurfaces().stream().filter(s -> s.isHole() == HoleStatus.HOLE).collect(Collectors.toList());

            List<AbstractShape> allPlainPolygons = FusionHelper.getFusionResultSummits(noHoles.stream().map(s -> new AbstractShape(s.getSummits())).collect(Collectors.toList()));

            List<Tile> allCuts = new ArrayList<>();

            for (AbstractShape plainPolygon: allPlainPolygons) {
                List<Segment> surfaceSegments = Segment.toSegments(plainPolygon.summits);
                allCuts = allCuts.stream().flatMap(t -> cutOneTile(surfaceSegments, t).stream()).collect(Collectors.toList());
            }

            for (Surface hole: holes) {
                allCuts.addAll(cutTilesThatNeedToBeCut(hole, tiles));
            }

            return allCuts;
        }

        List<Segment> surfaceSegments = Segment.toSegments(surface.getSummits());
        return tiles.stream().flatMap(t -> cutOneTile(surfaceSegments, t).stream()).collect(Collectors.toList());
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

    private List<Tile> cutOneTile(List<Segment> surfaceSegments, Tile tileToCut) {
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

        // TODO: attention il y a peut-être deux différents boutes de segments qui rentrent dans la tuile... ici on suppose qu'il n'y a qu'un seul pick
        if (segmentsThatPartiallyCutTheTile.size() >= 2) {
            return tileToCut.cut(segmentsThatPartiallyCutTheTile);
        }
        return Arrays.asList(tileToCut);
    }

}
