package utils;

import javafx.scene.shape.*;

import java.util.ArrayList;
import java.util.List;

import utils.Point;

public class FusionHelper {

    public static List<Point> getResultSummits(List<List<utils.Point>> allListOfPoints) {
        List<Point> first = allListOfPoints.get(0);

        List<Point> resultantSummits = first;
        for (List<Point> fs : allListOfPoints) {
            if (fs == first) {
                continue;
            }

            resultantSummits = getResultSummits(resultantSummits, fs);
        }
        return resultantSummits;
    }

    public static List<Point> getResultSummits(List<Point> summits1, List<Point> summits2) {
        Polygon polygon1 = new Polygon();
        Polygon polygon2 = new Polygon();
        List<Point> combinedPoly= new ArrayList<>();

        for(Point p : summits1) {
            polygon1.getPoints().addAll(p.x, p.y);
        }

        for(Point p : summits2) {
            polygon2.getPoints().addAll(p.x, p.y);
        }

        Path p3 = (Path) Polygon.union(polygon1, polygon2);

        for(PathElement el : p3.getElements()){
            if(el instanceof MoveTo) {
                MoveTo mt = (MoveTo) el;

                Point abstractPoint = new Point(mt.getX(), mt.getY());
                combinedPoly.add(abstractPoint);
            }
            if(el instanceof LineTo) {
                LineTo lt = (LineTo) el;
                Point abstractPoint = new Point(lt.getX(), lt.getY());
                combinedPoly.add(abstractPoint);
            }
        }

        return combinedPoly;
    }
}
