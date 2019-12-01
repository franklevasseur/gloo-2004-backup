package utils;

import javafx.scene.shape.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class FusionHelper {

    public static List<AbstractShape> getFusionResultSummits(List<AbstractShape> allAbstractShapes) {

        List<AbstractShape> sortedAbstractShape = sortList(allAbstractShapes);
        AbstractShape first = sortedAbstractShape.get(0);

        List<AbstractShape> resultantShapes = Arrays.asList(first);
        for (AbstractShape fs : sortedAbstractShape) {
            if (fs == first) {
                continue;
            }

            resultantShapes = getFusionResultSummits(resultantShapes, fs);
        }
        return resultantShapes;
    }

    public static List<AbstractShape> getFusionResultSummits(List<AbstractShape> accumulator, AbstractShape shape2) {

        if (accumulator.size() < 2 && accumulator.get(0).toSubstract) {
            throw new RuntimeException("Mon gars, j'px pas soustraire quelquechose à un trou esti");
        }

        Polygon polygon1 = new Polygon();
        Polygon polygon2 = new Polygon();
        List<Point> combinedPoly= new ArrayList<>();

        for(Point p : ShapeHelper.getFlattedSummits(accumulator)) {
            polygon1.getPoints().addAll(p.x, p.y);
        }

        for(Point p : shape2.summits) {
            polygon2.getPoints().addAll(p.x, p.y);
        }

        Path p3 = shape2.toSubstract ? (Path) Polygon.subtract(polygon1, polygon2) : (Path) Polygon.union(polygon1, polygon2);

        List<AbstractShape> shapes = new ArrayList<>();

        for(PathElement el : p3.getElements()) {
            if(el instanceof MoveTo) {

                if (combinedPoly.size() > 3) {
                    shapes.add(new AbstractShape(combinedPoly));
                    combinedPoly = new ArrayList<>();
                }

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

        if (combinedPoly.size() > 3) {
            shapes.add(new AbstractShape(combinedPoly));
        }

        return shapes;
    }

    private static List<AbstractShape> sortList(List<AbstractShape> surfaces) {

        List<AbstractShape> laTabarnakDeCalisseDeListeQuonVaRetournerAFinDeLestiDeFonction = new ArrayList<>();
        List<AbstractShape> lautreFuckingListeDeTrouEstiDeLaid = new ArrayList<>();

        for (AbstractShape s : surfaces) {
            if (!s.toSubstract) {
                laTabarnakDeCalisseDeListeQuonVaRetournerAFinDeLestiDeFonction.add(s);
            } else {
                lautreFuckingListeDeTrouEstiDeLaid.add(s);
            }
        }

        laTabarnakDeCalisseDeListeQuonVaRetournerAFinDeLestiDeFonction.addAll(lautreFuckingListeDeTrouEstiDeLaid);
        return laTabarnakDeCalisseDeListeQuonVaRetournerAFinDeLestiDeFonction;
    }
}
