package sample;

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

import gui.SurfaceUI;
import gui.SelectionManager;

public class UiController implements Initializable {

    public Pane drawingSection;

    private List<SurfaceUI> allSurfaces = new LinkedList<SurfaceUI>();
    private SelectionManager selectionManager = new SelectionManager();

    // state variables to make coherent state machine
    private boolean stateCurrentlyCreatingSurface = false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("Initialization...");
    }

    public void handleKeyPressed(KeyEvent e) {
        if(e.getCode() == KeyCode.DELETE) {
            List<SurfaceUI> selectedSurfaces = selectionManager.getSelectedSurfaces();
            List<Node> selectedNodes = selectedSurfaces.stream().map(SurfaceUI::getNode).collect(Collectors.toList());

            drawingSection.getChildren().removeIf(selectedNodes::contains);
            allSurfaces.removeIf(selectedSurfaces::contains);
        }
    }

    public void onDrawingSectionClicked(MouseEvent e) {
        if (stateCurrentlyCreatingSurface) {
            drawingSection.setCursor(Cursor.DEFAULT);
            stateCurrentlyCreatingSurface = false;

            SurfaceUI newSurface = createSurfaceHere(e, 40, 40);

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

    private SurfaceUI createSurfaceHere(MouseEvent e, double width, double height) {

        double x = e.getX() - (width / 2);
        double y = e.getY() - (height / 2);

        SurfaceUI newSurface = new SurfaceUI(x, y, width, height, selectionManager);

        return newSurface;
    }
}
