package sample;

import application.*;
import gui.RectangleSurfaceUI;
import gui.SelectionManager;

import gui.SurfaceUI;
import gui.ZoomManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import utils.Id;
import utils.Point;
import utils.RectangleHelper;
import utils.RectangleInfo;

import java.net.URL;
import java.util.*;

public class UiController implements Initializable {

    private ObservableList<String> possibleColorList = FXCollections.observableArrayList("Green","Blue","Orange","Black","White","red");
    public Pane drawingSection;

    // surface properties inputs
    public TextField tileHeightInputbox;
    public TextField tileWidthInputbox;
    public TextField sealWidthInputBox;
    public TextField surfaceHeightInputBox;
    public TextField surfaceWidthInputBox;

    public ChoiceBox surfaceColorChoiceBox;
    public ChoiceBox sealColorChoiceBox;
    @FXML
    private Button applyButton;

    private List<SurfaceUI> allSurfaces = new ArrayList<>();
    private SelectionManager selectionManager = new SelectionManager();
    private ZoomManager zoomManager = new ZoomManager();

    // state variables to make coherent state machine
    private boolean stateCurrentlyCreatingSurface = false;

    private Controller domainController = Controller.getInstance();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        System.out.println("Initialization...");
        //TODO: Frank check ça
        surfaceColorChoiceBox.setItems(possibleColorList);
        sealColorChoiceBox.setItems(possibleColorList);


    }

    @FXML
    public void editSurface(ActionEvent event) {
        // va chercher la ou les surfaces sélectionnées avec le selectionManager (tu en a un dans cette classe-ci)
        // tu vas faire les modifications sur la surface (SurfaceUI) avec des setteurs
        // tu vas ajouter des affaires dans la méthode toDto() de cette classe lèa (SurfaceUI)
        // tu vas anoncer au controlleur du domaine que tu viens de modifier une surface
        List<SurfaceUI> selectedSurfaces = selectionManager.getSelectedSurfaces();

        //Modification du Height d'une tuile
        for(SurfaceUI chosenSurface: selectedSurfaces){

            //new Tile Height
            CharSequence tileHeightInput = this.tileHeightInputbox.getCharacters();
            double newTileHeight = Double.parseDouble(tileHeightInput.toString());
            //new Tile Width
            CharSequence tileWidthInput = this.tileWidthInputbox.getCharacters();
            double newTileWidth = Double.parseDouble(tileWidthInput.toString());
            //new Seal Width
            CharSequence sealWidthInput = this.sealWidthInputBox.getCharacters();
            double newSealWidth = Double.parseDouble(sealWidthInput.toString());
            //new surface height
            CharSequence surfaceHeightInput = this.surfaceHeightInputBox.getCharacters();
            double newsurfaceHeight = Double.parseDouble(surfaceHeightInput.toString());
            //new surface width
            CharSequence surfaceWidthInput = this.surfaceWidthInputBox.getCharacters();
            double newSurfaceWidth = Double.parseDouble(surfaceWidthInput.toString());

            chosenSurface.hideTiles();
            //Changer seal width et couleur
            SealsInfoDto sealsInfoDto = chosenSurface.getSealsInfo();
            sealsInfoDto.sealWidth = newSealWidth;
            sealsInfoDto.sealColor = sealColorChoiceBox.getValue().toString();
            chosenSurface.setSealsInfo(sealsInfoDto);

            TileDto masterTile = chosenSurface.getMasterTile();
            RectangleInfo masterTileRect = RectangleHelper.summitsToRectangleInfo(masterTile.summits);

            masterTileRect.width = newTileWidth;
            masterTileRect.height = newTileHeight;
            masterTile.summits = RectangleHelper.rectangleInfoToSummits(masterTileRect.topLeftCorner, masterTileRect.width, masterTileRect.height);
            chosenSurface.setMasterTile(masterTile);



            // TODO: mettre un pattern qui vient de la gui + les bonne informations de sealing
            domainController.fillSurface(chosenSurface.toDto(), chosenSurface.getMasterTile(), PatternDto.DEFAULT, sealsInfoDto);
        }

        //Modifier Seal color-
        //Modifier Material
        //Modifier Tile Pattern
        //Modifier Dimension de surface-
        //Modifier Surface color-


        this.renderFromProject();
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
