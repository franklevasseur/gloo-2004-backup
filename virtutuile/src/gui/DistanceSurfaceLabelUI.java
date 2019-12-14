package gui;

import application.SurfaceDto;
import utils.AbstractShape;
import utils.Point;
import utils.ShapeHelper;

import java.util.List;

import static java.lang.Math.sqrt;

public class DistanceSurfaceLabelUI {
    private String distanceTwoSurfaces;

    public String distanceSurface(List<SurfaceDto> selectedSurfaces) {

        if (selectedSurfaces.size() == 2) {
            SurfaceDto firstSurface = selectedSurfaces.get(0);
            SurfaceDto secondSurface = selectedSurfaces.get(1);

            Double distance = this.calculateDistance(firstSurface, secondSurface);
            this.distanceTwoSurfaces = distance.toString();
            return distanceTwoSurfaces;
        }
        return "";
    }

    private Double calculateDistance(SurfaceDto firstSurface, SurfaceDto secondSurface) {

        Point firstTopLeft = ShapeHelper.getTopLeftCorner(new AbstractShape(firstSurface.summits, false));
        Point secondTopLeft = ShapeHelper.getTopLeftCorner(new AbstractShape(secondSurface.summits, false));

        double xF = firstTopLeft.x;
        double yF = firstTopLeft.y;
        double xS = secondTopLeft.x;
        double yS = secondTopLeft.y;

        Double distance = sqrt(((xF - xS) * (xF - xS) + (yF - yS) * (yF - yS)));
        return distance;
    }
}
