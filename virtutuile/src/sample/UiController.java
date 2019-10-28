package sample;

import application.ProjectDto;
import application.SurfaceDto;
import gui.RectangleSurfaceUI;
import gui.SelectionManager;

import application.Controller;

import gui.SurfaceUI;
import gui.ZoomManager;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import utils.Id;
import utils.Point;
import utils.RectangleHelper;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class UiController implements Initializable {

    public Pane drawingSection;

    private List<SurfaceUI> allSurfaces = new ArrayList<SurfaceUI>();
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

            createSurfaceHere(e, 40, 40);

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

    private void removeSelectedSurfaces() {
        List<SurfaceUI> selectedSurfaces = selectionManager.getSelectedSurfaces();
        List<Node> selectedNodes = selectedSurfaces.stream()
                .peek(SurfaceUI::unselect)
                .peek(s -> domainController.removeSurface(s.toDto()))
                .map(SurfaceUI::getNode).collect(Collectors.toList());

        drawingSection.getChildren().removeIf(selectedNodes::contains);
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
        surface.isRectangular = true;
        surface.summits = RectangleHelper.rectangleInfoToSummits(new Point(x, y), width, height);

        domainController.createSurface(surface);
    }

    private void renderFromProject() {

        this.clearDrawings();

        ProjectDto project = this.domainController.getProject();
        for (SurfaceDto surface: project.surfaces) {
            this.displaySurface(surface);
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
        this.drawingSection.getChildren().add(surfaceUi.getNode());
    }
}
