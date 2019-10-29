package sample;

import application.*;
import gui.RectangleSurfaceUI;
import gui.SelectionManager;

import gui.SurfaceUI;
import gui.ZoomManager;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.scene.Cursor;
import javafx.scene.input.*;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.transform.Scale;
import utils.Id;
import utils.Point;
import utils.RectangleHelper;

import java.net.URL;
import java.util.*;

public class UiController implements Initializable {

    public Pane pane;
    public Pane drawingSection;
    private Circle originIndicator;

    private List<SurfaceUI> allSurfaces = new ArrayList<>();
    private SelectionManager selectionManager = new SelectionManager();
    private ZoomManager zoomManager = new ZoomManager();

    // state variables to make coherent state machine
    private boolean stateCurrentlyCreatingSurface = false;
    private boolean stateEnableZooming = false;

    private Controller domainController = Controller.getInstance();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("Initialization...");

        originIndicator = new Circle();
        originIndicator.setFill(Color.RED);
        originIndicator.setRadius(10);

//        drawingSection.setStyle("-fx-background-color: #0ff");
        drawingSection.setPrefHeight(100);
        drawingSection.setPrefWidth(100);

        drawingSection.getChildren().add(originIndicator);
    }

    public void handleZoom(ScrollEvent event) {
        if (stateEnableZooming) {
            double zoom_fac = 1.05;
            double delta_y = event.getDeltaY();

            if (delta_y < 0) {
                zoom_fac = 1.9 - zoom_fac;
            }

            Point cursorCoord = this.getPointInReferenceToOrigin(new Point(event.getX(), event.getY()));

            Scale newScale = new Scale();
            newScale.setPivotX(cursorCoord.x);
            newScale.setPivotY(cursorCoord.y);
            newScale.setX(zoom_fac);
            newScale.setY(zoom_fac);

            drawingSection.getTransforms().add(newScale);
            zoomManager.zoomBy(zoom_fac);
        }
        event.consume();
    }

    public void resetZoom() {
        drawingSection.getTransforms().clear();
        zoomManager.resetZoom();
    }

    public void handleKeyPressed(KeyEvent e) {
        if(e.getCode() == KeyCode.DELETE) {
            removeSelectedSurfaces();
        }
        if(e.getCode() == KeyCode.CONTROL) {
            selectionManager.allowMultipleSelection();
            stateEnableZooming = true;
        }
        if(e.getCode() == KeyCode.ALT) {
            resetZoom();
        }
    }

    public void handleKeyReleased(KeyEvent e) {
        if(e.getCode() == KeyCode.CONTROL) {
            selectionManager.disableMultipleSelection();
            stateEnableZooming = false;
        }
    }

    public void onPaneClicked(MouseEvent e) {

        Point clikCoord = this.getPointInReferenceToOrigin(new Point(e.getX(), e.getY()));

        System.out.println(String.format("click : (%f, %f)", clikCoord.x, clikCoord.y));
        if (stateCurrentlyCreatingSurface) {
            pane.setCursor(Cursor.DEFAULT);
            stateCurrentlyCreatingSurface = false;

            createSurfaceHere(new Point(clikCoord.x, clikCoord.y), 200, 200);

            this.renderFromProject();
        }
        selectionManager.unselectAll();
    }

    private Point getPointInReferenceToOrigin(Point pointInReferenceToPane) {
        Bounds bound = originIndicator.getBoundsInParent();
        Bounds actual = drawingSection.localToParent(bound);
        double xOrigin = actual.getCenterX();
        double yOrigin = actual.getCenterY();

        double xFromOrigin = (pointInReferenceToPane.x - xOrigin) / zoomManager.getCurrentScale();
        double yFromOrigin = (pointInReferenceToPane.y - yOrigin) / zoomManager.getCurrentScale();

        return new Point(xFromOrigin, yFromOrigin);
    }

    public void onCreateSurfaceSelected() {
        if(!stateCurrentlyCreatingSurface) {
            stateCurrentlyCreatingSurface = true;
            pane.setCursor(Cursor.CROSSHAIR);
        }
    }

    public void fillSelectedSurfaceWithTiles() {
        List<SurfaceUI> selectedSurfaces = this.selectionManager.getSelectedSurfaces();

        for (SurfaceUI surface: selectedSurfaces) {
            this.domainController.fillSurfaceWithDefaults(surface.toDto());
        }

        renderFromProject();
    }

    private void removeSelectedSurfaces() {
        List<SurfaceUI> selectedSurfaces = selectionManager.getSelectedSurfaces();
        selectedSurfaces.forEach(SurfaceUI::remove);
        allSurfaces.removeIf(selectedSurfaces::contains);
    }

    private void createSurfaceHere(Point location, double widthPixels, double heightPixels) {

        double xPixels = location.x - (widthPixels / 2);
        double yPixels = location.y - (heightPixels / 2);

        double x = zoomManager.pixelsToMeters(xPixels);
        double y = zoomManager.pixelsToMeters(yPixels);
        double width = zoomManager.pixelsToMeters(widthPixels);
        double height = zoomManager.pixelsToMeters(heightPixels);

        SurfaceDto surface = new SurfaceDto();
        surface.id = new Id();
        surface.isHole = true;
        surface.isRectangular = true;
        surface.summits = RectangleHelper.rectangleInfoToSummits(new Point(x, y), width, height);

        domainController.createSurface(surface);
    }

    private void renderFromProject() {

        this.clearDrawings();

        ProjectDto project = this.domainController.getProject();
        if (project.surfaces != null) {
            for (SurfaceDto surface: project.surfaces) {
                this.displaySurface(surface);
            }
        }
    }

    private void clearDrawings() {
        this.allSurfaces.clear();
        this.selectionManager.unselectAll();
        this.drawingSection.getChildren().clear();
        this.drawingSection.getChildren().add(originIndicator);
    }

    private void displaySurface(SurfaceDto surfaceDto) {
        RectangleSurfaceUI surfaceUi = new RectangleSurfaceUI(surfaceDto, zoomManager, selectionManager, drawingSection);
        this.allSurfaces.add(surfaceUi);
    }
}
