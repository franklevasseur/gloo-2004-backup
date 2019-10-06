package sample;

import gui.RectangleSurfaceUI;
import gui.SelectionManager;
import gui.PixelPoint;

import application.Controller;

import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class UiController implements Initializable {

    public Pane drawingSection;

    private List<RectangleSurfaceUI> allSurfaces = new LinkedList<>();
    private SelectionManager selectionManager = new SelectionManager();

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

            RectangleSurfaceUI newSurface = createSurfaceHere(e, 40, 40);

            allSurfaces.add(newSurface);
            drawingSection.getChildren().addAll(newSurface.getNode());
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
        List<RectangleSurfaceUI> selectedSurfaces = selectionManager.getSelectedSurfaces();
        List<Node> selectedNodes = selectedSurfaces.stream()
                .peek(RectangleSurfaceUI::unselect)
                .map(RectangleSurfaceUI::getNode).collect(Collectors.toList());

        drawingSection.getChildren().removeIf(selectedNodes::contains);
        allSurfaces.removeIf(selectedSurfaces::contains);
    }

    private RectangleSurfaceUI createSurfaceHere(MouseEvent e, double width, double height) {

        double x = e.getX() - (width / 2);
        double y = e.getY() - (height / 2);
        PixelPoint topLeftCorner = new PixelPoint(x, y);

        return new RectangleSurfaceUI(topLeftCorner, width, height, selectionManager, drawingSection);
    }
}
