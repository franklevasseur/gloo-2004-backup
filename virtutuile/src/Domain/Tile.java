package Domain;

import utils.AbstractShape;
import utils.Point;
import utils.Segment;
import utils.ShapeHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Tile {
    private List<Point> summits = new ArrayList<>();
    private Material material;

    private boolean isCut = false;

    public Tile() {
        System.out.println("WARING, ce constructeur est deprecated, on veut le tuer");
    }

    public Tile(List<Point> pSummit, Material pMaterial) {
        summits = pSummit;
        material = pMaterial;
    }

    public Tile(List<Point> pSummit, Material pMaterial, boolean isCut) {
        summits = pSummit;
        material = pMaterial;
        this.isCut = isCut;
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

    public boolean isCut() {
        return isCut;
    }

    public List<Tile> cutSideToSide(Segment cuttingSegment) {

        List<Segment> firstHalfSegments = new ArrayList<>();
        List<Segment> secondHalfSegments = new ArrayList<>();

        List<Segment> fullTileSegments = Segment.toSegments(this.getSummits());

        List<Point> intersections = Segment.findIntersection(cuttingSegment, fullTileSegments);
        if (intersections.size() != 2) {
            throw new RuntimeException("Icitte on coupe bord en bord, call une autre méthode si t'es pas content");
        }

        List<Segment> currentHalf = firstHalfSegments;
        List<Segment> otherHalf = secondHalfSegments;

        for (Segment s : fullTileSegments) {
            Point intersection = cuttingSegment.intersect(s);
            if (intersection == null) {
                currentHalf.add(s);
                continue;
            }

            Point otherIntersection = intersection == intersections.get(0) ? intersections.get(1) : intersections.get(0);

            currentHalf.add(new Segment(s.pt1, intersection));
            currentHalf.add(new Segment(intersection, otherIntersection));

            otherHalf.add(new Segment(intersection, s.pt2));

            // switching half...
            List<Segment> tmp;
            tmp = currentHalf;
            currentHalf = otherHalf;
            otherHalf = tmp;
        }

        List<Point> firstHalfSummits = Point.fromSegments(firstHalfSegments);
        List<Point> secondHalfSummits = Point.fromSegments(secondHalfSegments);

        return Arrays.asList(new Tile(firstHalfSummits, this.material, true), new Tile(secondHalfSummits, this.material, true));
    }
}
