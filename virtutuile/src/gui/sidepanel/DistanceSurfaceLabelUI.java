package gui.sidepanel;

import application.SurfaceDto;
import gui.ZoomManager;
import javafx.scene.control.Label;
import utils.AbstractShape;
import utils.Point;
import utils.ShapeHelper;

import java.util.List;

import static java.lang.Math.sqrt;

public class DistanceSurfaceLabelUI {

    private Label presentationLabel;
    private Label distanceLabel;
    private ZoomManager zoomManager;

    public DistanceSurfaceLabelUI(Label presentationLabel, Label distanceLabel, ZoomManager zoomManager) {
        this.presentationLabel = presentationLabel;
        this.distanceLabel = distanceLabel;
        this.zoomManager = zoomManager;
    }

    public void updateDistanceSurface(List<SurfaceDto> selectedSurfaces, boolean metric) {

        if (selectedSurfaces.size() == 2) {
            SurfaceDto firstSurface = selectedSurfaces.get(0);
            SurfaceDto secondSurface = selectedSurfaces.get(1);

            Double distance = this.calculateDistance(firstSurface, secondSurface, metric);

            InputBoxHelper helper = new InputBoxHelper(metric, zoomManager);
            String displayString = helper.formatMetric(distance);

            distanceLabel.setText(displayString);
            presentationLabel.setText("Distance between two surfaces: ");
            return;
        }

        eraseDistance();
    }

    public void eraseDistance() {
        presentationLabel.setText("");
        distanceLabel.setText("");
    }

    private Double calculateDistance(SurfaceDto firstSurface, SurfaceDto secondSurface, boolean metric) {

        Point firstTopLeft = ShapeHelper.getTopLeftCorner(new AbstractShape(firstSurface.summits, false));
        Point secondTopLeft = ShapeHelper.getTopLeftCorner(new AbstractShape(secondSurface.summits, false));

        double xF = firstTopLeft.x;
        double yF = firstTopLeft.y;
        double xS = secondTopLeft.x;
        double yS = secondTopLeft.y;

        return sqrt(((xF - xS) * (xF - xS) + (yF - yS) * (yF - yS)));
    }
}
