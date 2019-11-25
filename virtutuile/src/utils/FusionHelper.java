package utils;

import javafx.scene.shape.*;

import java.util.ArrayList;
import java.util.List;

public class FusionHelper {

    public static AbstractShape getFusionResultSummits(List<AbstractShape> allAbstractShapes) {

        List<AbstractShape> sortedAbstractShape = sortFuckingStupidFuckingListDeTabarnakQueJavaCestUnEstiDeLangageDeCulVaChier(allAbstractShapes);
        AbstractShape first = sortedAbstractShape.get(0);

        AbstractShape resultantShape = first;
        for (AbstractShape fs : sortedAbstractShape) {
            if (fs == first) {
                continue;
            }

            resultantShape = getFusionResultSummits(resultantShape, fs);
        }
        return resultantShape;
    }

    public static AbstractShape getFusionResultSummits(AbstractShape shape1, AbstractShape shape2) {

        if (shape1.toSubstract) {
            throw new RuntimeException("Mon gars, j'px pas soustraire quelquechose à un trou esti");
        }

        Polygon polygon1 = new Polygon();
        Polygon polygon2 = new Polygon();
        List<Point> combinedPoly= new ArrayList<>();

        for(Point p : shape1.summits) {
            polygon1.getPoints().addAll(p.x, p.y);
        }

        for(Point p : shape2.summits) {
            polygon2.getPoints().addAll(p.x, p.y);
        }

        Path p3 = shape2.toSubstract ? (Path) Polygon.subtract(polygon1, polygon2) : (Path) Polygon.union(polygon1, polygon2);

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

        return new AbstractShape(combinedPoly, false);
    }

    private static List<AbstractShape> sortFuckingStupidFuckingListDeTabarnakQueJavaCestUnEstiDeLangageDeCulVaChier(List<AbstractShape> surfaces) {

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
