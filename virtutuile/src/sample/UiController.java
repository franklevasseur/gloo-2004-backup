package sample;

import application.*;
import gui.RectangleSurfaceUI;
import gui.SelectionManager;

import gui.SurfaceUI;
import gui.ZoomManager;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import utils.Id;
import utils.Point;
import utils.RectangleHelper;

import java.net.URL;
import java.util.*;

public class UiController implements Initializable {

    public Pane drawingSection;

    private List<SurfaceUI> allSurfaces = new ArrayList<>();
    private SelectionManager selectionManager = new SelectionManager();
    private ZoomManager zoomManager = new ZoomManager();

    // state variables to make coherent state machine
    private boolean stateCurrentlyCreatingSurface = false;

    private Controller domainController = Controller.getInstance();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("Initialization...");
    }

    public void handleKeyPressed(KeyEvent e) {
        if(e.getCode() == KeyCode.DELETE) {
            removeSelectedSurfaces();
        }
        if(e.getCode() == KeyCode.CONTROL) {
            selectionManager.allowMultipleSelection();
        }
    }

    public void handleKeyReleased(KeyEvent e) {
        if(e.getCode() == KeyCode.CONTROL) {
            selectionManager.disableMultipleSelection();
        }
    }

    public void onDrawingSectionClicked(MouseEvent e) {
        if (stateCurrentlyCreatingSurface) {
            drawingSection.setCursor(Cursor.DEFAULT);
            stateCurrentlyCreatingSurface = false;

            createSurfaceHere(e, 200, 200);

            this.renderFromProject();
        }
        selectionManager.unselectAll();
    }

    public void onCreateSurfaceSelected() {
        if(!stateCurrentlyCreatingSurface) {
            stateCurrentlyCreatingSurface = true;
            drawingSection.setCursor(Cursor.CROSSHAIR);
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

    private void createSurfaceHere(MouseEvent e, double widthPixels, double heightPixels) {

        double xPixels = e.getX() - (widthPixels / 2);
        double yPixels = e.getY() - (heightPixels / 2);

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
    }

    private void displaySurface(SurfaceDto surfaceDto) {
        RectangleSurfaceUI surfaceUi = new RectangleSurfaceUI(surfaceDto, zoomManager, selectionManager, drawingSection);
        this.allSurfaces.add(surfaceUi);
    }
}
