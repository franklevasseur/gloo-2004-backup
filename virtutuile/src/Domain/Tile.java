package Domain;

import utils.AbstractShape;
import utils.Point;
import utils.Segment;
import utils.ShapeHelper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Tile implements Serializable {
    private List<Point> summits = new ArrayList<>();
    private Material material;

    private boolean isMasterTile = false;

    public Tile(List<Point> pSummit, Material pMaterial, boolean isMasterTile) {
        this(pSummit, pMaterial);
        this.isMasterTile = isMasterTile;
    }

    public Tile(List<Point> pSummit, Material pMaterial) {
        summits = pSummit;
        material = pMaterial;
        isMasterTile = false;
    }

    public double getHeight() {
        return ShapeHelper.getHeight(toAbstractShape());
    }

    private AbstractShape toAbstractShape() {
        return new AbstractShape(summits);
    }

    public double getWidth() {
        return ShapeHelper.getWidth(toAbstractShape());
    }

    public List<Point> getSummits() {
        return this.summits;
    }

    public void setSummits(ArrayList<Point> summits) {
        this.summits = summits;
    }

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public void setSummits(List<Point> summits) {
        this.summits = summits;
    }

    public List<Tile> cutSideToSide(Segment cuttingSegment) {
        return this.cutSideToSide(cuttingSegment, false);
    }

    public Tile deepCopy() {
        List<Point> summitsCpy = new ArrayList<>(summits.size());
        for (Point summit : summits) {
            summitsCpy.add(summit.deepCpy());
        }
        return new Tile(summitsCpy, this.material, this.isMasterTile);
    }

    List<Tile> cutSideToSide(Segment cuttingSegment, boolean extendCuttingEdge) {

        List<Segment> firstHalfSegments = new ArrayList<>();
        List<Segment> secondHalfSegments = new ArrayList<>();

        List<Segment> fullTileSegments = Segment.fromPoints(this.summits);

        List<Point> intersections = findIntersections(cuttingSegment, extendCuttingEdge);

        if (intersections.size() == 1) {
            // possible si l'intersection est sur un sommet
            return Arrays.asList(this);
        }
        if (intersections.size() > 2) {
//            System.out.println(String.format("Icitte on coupe bord en bord, call une autre méthode si t'es pas content... La t'as %d intersections", intersections.size()));
            return Arrays.asList(this);
        }

        List<Segment> currentHalf = firstHalfSegments;
        List<Segment> otherHalf = secondHalfSegments;

        List<Point> alreadyTaggedIntersection = new ArrayList<>();
        for (Segment s : fullTileSegments) {
            // on prend toujours la méthode extend finalement, j'ai pas le temps de mettre ca propre, j'suis désolé!
            Point intersection = true ? cuttingSegment.extendAndIntersect(s, Point.DOUBLE_TOLERANCE) : cuttingSegment.intersect(s, Point.DOUBLE_TOLERANCE);
            if (intersection == null
                    || alreadyTaggedIntersection.size() >= intersections.size()
                    || !alreadyTaggedIntersection.stream().noneMatch(i -> i.isInRange(intersection, Point.DOUBLE_TOLERANCE))) {
                currentHalf.add(s);
                continue;
            }

            alreadyTaggedIntersection.add(intersection);

            Point otherIntersection = intersection.isSame(intersections.get(0)) ? intersections.get(1) : intersections.get(0);

            currentHalf.add(new Segment(s.pt1, intersection));
            currentHalf.add(new Segment(intersection, otherIntersection));

            otherHalf.add(new Segment(intersection, s.pt2));

            // switching half...
            List<Segment> tmp;
            tmp = currentHalf;
            currentHalf = otherHalf;
            otherHalf = tmp;
        }

        List<Point> firstHalfSummits = Point.removeDuplicatedSummits(Point.fromSegments(firstHalfSegments), Point.DOUBLE_TOLERANCE);
        List<Point> secondHalfSummits = Point.removeDuplicatedSummits(Point.fromSegments(secondHalfSegments), Point.DOUBLE_TOLERANCE);

        if (firstHalfSummits.size() < 3 || secondHalfSummits.size() < 3) {
//            System.out.println(String.format("WHAT THE FUCKKKKKKKKKKKK, firstHalf = %d, secondHalf = %d", firstHalfSummits.size(), secondHalfSummits.size()));
            return Arrays.asList(this);
        }

        return Arrays.asList(new Tile(firstHalfSummits, this.material, this.isMasterTile), new Tile(secondHalfSummits, this.material, this.isMasterTile));
    }

    public List<Tile> doOneCut(Segment cuttingSegment) {
        List<Segment> tileSegments = Segment.fromPoints(summits);
        int nInterSections = findIntersections(cuttingSegment, false).size();
        if (nInterSections == 2) {
            return cutSideToSide(cuttingSegment, false);
        } else if (nInterSections == 1 || cuttingSegment.isInside(tileSegments, false)) {
            return cutSideToSide(cuttingSegment, true);
        }
        return Arrays.asList(this);
    }

    List<Point> findIntersections(Segment segment, boolean extendSegment) {
        List<Segment> tileSegments = Segment.fromPoints(this.summits);
        List<Point> currentIntersections = new ArrayList<>();
        for (Segment seg : tileSegments) {
            Point intersection = extendSegment ? segment.extendAndIntersect(seg, Point.DOUBLE_TOLERANCE) : segment.intersect(seg, Point.DOUBLE_TOLERANCE);
            if (intersection != null && currentIntersections.stream().noneMatch(i -> i.isInRange(intersection, Point.DOUBLE_TOLERANCE))) {
                currentIntersections.add(intersection);
            }
        }
        return currentIntersections;
    }

    public boolean isMasterTile() {
        return isMasterTile;
    }
}
