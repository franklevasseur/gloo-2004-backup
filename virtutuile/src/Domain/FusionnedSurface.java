package Domain;

import javafx.scene.shape.*;

import java.util.ArrayList;
import java.util.List;

public class FusionnedSurface extends Surface {

    private List<Surface> fusionnedSurfaces;

    public FusionnedSurface(List<Surface> fusionnedSurfaces) {
        super(false, getResultSummits(fusionnedSurfaces), false);
        this.fusionnedSurfaces = fusionnedSurfaces;
    }

    public List<Surface> getFusionnedSurfaces() {
        return this.fusionnedSurfaces;
    }


    private static List<Point> getResultSummits(List<Surface> fusionnedSurfaces) {
        Surface first = fusionnedSurfaces.get(0);

        List<Point> resultantSummits = first.getSummits();
        for (Surface fs : fusionnedSurfaces) {
            if (fs == first) {
                continue;
            }

            resultantSummits = getResultSummits(resultantSummits, fs.getSummits());
        }
        return resultantSummits;
    }

    private static List<Point> getResultSummits(List<Point> summits1, List<Point> summits2) {
        Polygon polygon1 = new Polygon();
        Polygon polygon2 = new Polygon();
        List<Point> combinedPoly= new ArrayList<>();

        for(Point p : summits1) {
            polygon1.getPoints().addAll(p.getX().getValue(), p.getY().getValue());
        }

        for(Point p : summits2) {
            polygon2.getPoints().addAll(p.getX().getValue(), p.getY().getValue());
        }

        Path p3 = (Path) Polygon.union(polygon1, polygon2);

        for(PathElement el : p3.getElements()){
            if(el instanceof MoveTo) {
                MoveTo mt = (MoveTo) el;

                utils.Point abstractPoint = new utils.Point(mt.getX(), mt.getY());
                combinedPoly.add(new Point(abstractPoint));
            }
            if(el instanceof LineTo) {
                LineTo lt = (LineTo) el;
                utils.Point abstractPoint = new utils.Point(lt.getX(), lt.getY());
                combinedPoly.add(new Point(abstractPoint));
            }
        }

        return combinedPoly;
    }

    @Override
    public boolean isFusionned() {
        return true;
    }
}
