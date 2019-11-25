package sample;

import application.*;
import gui.*;

import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.Pane;
import javafx.scene.transform.Scale;
import utils.Id;
import utils.Point;
import utils.RectangleHelper;
import utils.RectangleInfo;

import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;
import java.lang.Math;

public class UiController implements Initializable {

    public Pane pane;
    public Pane drawingSection;

    // surface properties inputs
    public TextField tileHeightInputbox;
    public TextField tileWidthInputbox;
    public TextField sealWidthInputBox;
    public TextField surfaceHeightInputBox;
    public TextField surfaceWidthInputBox;
    public TextField surfacePositionXInputBox;
    public TextField surfacePositionYInputBox;

    public Label tileInfo;

    public ChoiceBox surfaceColorChoiceBox;
    public ChoiceBox sealColorChoiceBox;

    public CheckBox snapGridCheckBox;

    private List<SurfaceUI> allSurfaces = new ArrayList<>();
    private SelectionManager selectionManager;
    private ZoomManager zoomManager = new ZoomManager();
    private SnapGridUI snapGridUI;

    public Button undoButton;
    public Button redoButton;

    // state variables to make coherent state machine
    private boolean stateCurrentlyCreatingSurface = false;
    private boolean stateTopLeftCornerCreated = false;
    private boolean stateEnableZooming = false;

    private Point firstClickCoord;
    double reset = 0.0;

    private Controller domainController = Controller.getInstance();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("Initialization...");

        // Invisible Pane object that contains all other shapes
        // Needed to be invisible so zooming out of it would not expose its edge
        // Make it look like its infinite in size
        drawingSection.setPrefHeight(1);
        drawingSection.setPrefWidth(1);

        this.snapGridUI = new SnapGridUI(this.drawingSection);
        this.selectionManager = new SelectionManager(this::handleSelection);

        this.undoButton.setDisable(!this.domainController.undoAvailable());
        this.redoButton.setDisable(!this.domainController.redoAvailable());
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

            this.snapGridUI.renderForViewBox(this.getViewBoxSummits());
        }
        event.consume();
    }

    public void resetZoom() {
        drawingSection.getTransforms().clear();
        zoomManager.resetZoom();
        this.snapGridUI.renderForViewBox(this.getViewBoxSummits());
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

        Point clickCoord = this.getPointInReferenceToOrigin(new Point(e.getX(), e.getY()));

//        System.out.println(String.format("click : (%f, %f)", zoomManager.pixelsToMeters(clickCoord.x), zoomManager.pixelsToMeters(clickCoord.y)));
        if (stateCurrentlyCreatingSurface && !stateTopLeftCornerCreated) {

            firstClickCoord = new Point(clickCoord.x, clickCoord.y);

            stateTopLeftCornerCreated = true;
        }
        else if (stateCurrentlyCreatingSurface && stateTopLeftCornerCreated)
        {

            Point secondClickCoord = clickCoord;
            stateCurrentlyCreatingSurface = false;
            stateTopLeftCornerCreated = false;
            createSurfaceHere(new Point(firstClickCoord.x, firstClickCoord.y), new Point(secondClickCoord.x, secondClickCoord.y) );

            pane.setCursor(Cursor.DEFAULT);
            this.renderFromProject();
            selectionManager.unselectAll();
            hideRectangleInfo();
            firstClickCoord = null;
        }
    }

    public Void handleSelection(boolean isRectangle) {
        if (isRectangle) {
            afficherRectangleInfo();
        }
        return null;
    }

    public void editSurface() {
        List<SurfaceUI> selectedSurfaces = selectionManager.getSelectedSurfaces();

        //Modification du Height d'une tuile
//        for(SurfaceUI chosenSurface: selectedSurfaces) {
//
//        }
        if(selectedSurfaces.size() != 0){
            SurfaceUI chosenSurface = selectedSurfaces.get(0);
            NumberFormat format = NumberFormat.getInstance(Locale.FRANCE);
            try{

                //new Tile Height
                CharSequence tileHeightInput = this.tileHeightInputbox.getCharacters();
                Double newTileHeight = tileHeightInput.toString().equals("") ? null : format.parse(tileHeightInput.toString()).doubleValue();

                //new Tile Width
                CharSequence tileWidthInput = this.tileWidthInputbox.getCharacters();
                Double newTileWidth = tileWidthInput.toString().equals("") ? null : format.parse(tileWidthInput.toString()).doubleValue();

                //new Seal Width
                CharSequence sealWidthInput = this.sealWidthInputBox.getCharacters();
                Double newSealWidth = sealWidthInput.toString().equals("") ? null : format.parse(sealWidthInput.toString()).doubleValue();


                //new surface height
                CharSequence surfaceHeightInput = this.surfaceHeightInputBox.getCharacters();
                double newsurfaceHeight = format.parse(surfaceHeightInput.toString()).doubleValue();


                //new surface width
                CharSequence surfaceWidthInput = this.surfaceWidthInputBox.getCharacters();
                double newSurfaceWidth = format.parse(surfaceWidthInput.toString()).doubleValue();


                if (newSealWidth != null) {
                    //Changer seal width et couleur
                    SealsInfoDto sealsInfoDto = new SealsInfoDto();
                    sealsInfoDto.sealWidth = newSealWidth;
//                  sealsInfoDto.sealColor = sealColorChoiceBox.getValue().toString();
                    chosenSurface.setSealsInfo(sealsInfoDto);
                }

                if (newTileWidth != null && newTileHeight != null) {
                    TileDto masterTile = new TileDto();
                    RectangleInfo masterTileRect = new RectangleInfo(new Point(0,0 ), newTileWidth, newTileHeight);

                    masterTile.summits = RectangleHelper.rectangleInfoToSummits(masterTileRect.topLeftCorner, masterTileRect.width, masterTileRect.height);
                    chosenSurface.setMasterTile(masterTile);
                }
                //Changer la position de X et de y
                CharSequence positionXinput = surfacePositionXInputBox.getCharacters();
                CharSequence positionYinput = surfacePositionYInputBox.getCharacters();
                double newPositioinX = format.parse(positionXinput.toString()).doubleValue();
                double newPositionY = format.parse(positionYinput.toString()).doubleValue();
                Point position = new Point(newPositioinX,newPositionY);
                chosenSurface.setPosition(position);

                //surfacePosotoionXInputBox
                //surfacePosotoionYInputBox

                chosenSurface.setSize(newSurfaceWidth, newsurfaceHeight);
                this.domainController.updateSurface(chosenSurface.toDto());
                chosenSurface.fill();
                this.renderFromProject();
                hideRectangleInfo();
            }
            catch (ParseException e ) {
                afficherRectangleInfo();
            }
        }

    }

    private void afficherRectangleInfo(){
        List<SurfaceUI> selectedSurfaces = selectionManager.getSelectedSurfaces();
        SurfaceUI firstOne = selectedSurfaces.get(0);

        RectangleInfo rect = RectangleHelper.summitsToRectangleInfo(firstOne.toDto().summits);
        NumberFormat formatter = new DecimalFormat("#0.000");
        surfaceHeightInputBox.setText(formatter.format(rect.height));
        surfaceWidthInputBox.setText(formatter.format(rect.width));

        if (firstOne.getMasterTile() != null){
            RectangleInfo tileRect = RectangleHelper.summitsToRectangleInfo(firstOne.getMasterTile().summits);
            tileHeightInputbox.setText(formatter.format(tileRect.height));
            tileWidthInputbox.setText(formatter.format(tileRect.width));
        }

        if(firstOne.getSealsInfo() != null){
            sealWidthInputBox.setText(formatter.format(firstOne.getSealsInfo().sealWidth));
        }

        surfacePositionXInputBox.setText(formatter.format(rect.topLeftCorner.x));
        surfacePositionYInputBox.setText(formatter.format(rect.topLeftCorner.y));
//        surfaceColorChoiceBox;
//        sealColorChoiceBox;
    }

    private void hideRectangleInfo(){

        tileHeightInputbox.clear();
        tileWidthInputbox.clear();
        surfaceHeightInputBox.clear();
        surfaceWidthInputBox.clear();
        surfacePositionXInputBox.clear();
        surfacePositionYInputBox.clear();
    }

    private Point getPointInReferenceToOrigin(Point pointInReferenceToPane) {
        Bounds bound = snapGridUI.getOriginBounds();
        Bounds actual = drawingSection.localToParent(bound);

        double xOrigin = actual.getMaxX() - (actual.getWidth() / 2);
        double yOrigin = actual.getMaxY() - (actual.getHeight() / 2);

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

    public void snapGridToggle() {
        if (snapGridCheckBox.isSelected()) {
            this.snapGridUI.setVisibility(true);
            this.snapGridUI.renderForViewBox(this.getViewBoxSummits());
        } else {
            this.snapGridUI.setVisibility(false);
            this.snapGridUI.removeGrid();
        }
    }

    private List<Point> getViewBoxSummits() {
        List<Point> viewBorders = new ArrayList<>();

        Point zeroZero = new Point(0,0);
        Point topLeftViewPoint = this.getPointInReferenceToOrigin(zeroZero);
        Point topRightViewPoint = this.getPointInReferenceToOrigin(Point.translate(zeroZero, pane.getWidth(), 0));
        Point bottomLeftViewPoint = this.getPointInReferenceToOrigin(Point.translate(zeroZero, 0, pane.getHeight()));
        Point bottomRightViewPoint = this.getPointInReferenceToOrigin(Point.translate(zeroZero, pane.getWidth(), pane.getHeight()));

        viewBorders.add(topLeftViewPoint);
        viewBorders.add(topRightViewPoint);
        viewBorders.add(bottomLeftViewPoint);
        viewBorders.add(bottomRightViewPoint);
        return viewBorders;
    }

    public void fillSelectedSurfaceWithTiles() {
        List<SurfaceUI> selectedSurfaces = this.selectionManager.getSelectedSurfaces();

        for (SurfaceUI surface: selectedSurfaces) {
            surface.forceFill();
        }

        renderFromProject();
    }

    private void removeSelectedSurfaces() {
        List<SurfaceUI> selectedSurfaces = selectionManager.getSelectedSurfaces();
        drawingSection.getChildren().removeIf(selectedSurfaces.stream().map(s -> s.getNode()).collect(Collectors.toList())::contains);
        selectedSurfaces.forEach(SurfaceUI::delete);
        allSurfaces.removeIf(selectedSurfaces::contains);
        selectionManager.unselectAll();
    }

    private void createSurfaceHere(Point location1, Point location2) {

        double x1Pixels = location1.x;
        double y1Pixels = location1.y;
        double x2Pixels = location2.x;
        double y2Pixels = location2.y;

        double widthPixels = Math.abs(x2Pixels - x1Pixels);
        double heightPixels = Math.abs(y2Pixels - y1Pixels);

        Point desiredPoint1 = new Point(x1Pixels, y1Pixels);
        Point actualPoint1 = this.snapGridUI.isVisible() ? this.snapGridUI.getNearestGridPoint(desiredPoint1) : desiredPoint1;

        double x = zoomManager.pixelsToMeters(actualPoint1.x);
        double y = zoomManager.pixelsToMeters(actualPoint1.y);
        double width = zoomManager.pixelsToMeters(widthPixels);
        double height = zoomManager.pixelsToMeters(heightPixels);

        SurfaceDto surface = new SurfaceDto();
        surface.id = new Id();
        surface.isHole = false;
        surface.isRectangular = true;
        surface.summits = RectangleHelper.rectangleInfoToSummits(new Point(x, y), width, height);

        domainController.createSurface(surface);
    }

    private void renderFromProject() {

        this.clearDrawings();

        this.undoButton.setDisable(!this.domainController.undoAvailable());
        this.redoButton.setDisable(!this.domainController.redoAvailable());

        ProjectDto project = this.domainController.getProject();
        if (project.surfaces != null) {
            for (SurfaceDto surface: project.surfaces) {
                this.displaySurface(surface);
            }
        }
    }

    private void clearDrawings() {
        this.allSurfaces.forEach(SurfaceUI::hide);
        drawingSection.getChildren().removeIf(allSurfaces.stream().map(s -> s.getNode()).collect(Collectors.toList())::contains);
        this.selectionManager.unselectAll();
        this.allSurfaces.clear();
        this.snapGridUI.renderForViewBox(this.getViewBoxSummits());
    }

    private void displaySurface(SurfaceDto surfaceDto) {
        if (surfaceDto.isFusionned) {
            FusionedSurfaceUI surfaceUi = new FusionedSurfaceUI(zoomManager, selectionManager, snapGridUI, surfaceDto);
            this.drawingSection.getChildren().add(surfaceUi.getNode());
            this.allSurfaces.add(surfaceUi);
            return;
        }
        RectangleSurfaceUI surfaceUi = new RectangleSurfaceUI(surfaceDto,
                zoomManager,
                selectionManager,
                snapGridUI,
                this.tileInfo);
        this.drawingSection.getChildren().add(surfaceUi.getNode());
        this.allSurfaces.add(surfaceUi);
    }

    public void surfaceFusion() {
        if (this.selectionManager.getSelectedSurfaces().size() <= 0) {
            return;
        }

        List<SurfaceUI> selectedSurfaces = this.selectionManager.getSelectedSurfaces();
        this.domainController.fusionSurfaces(selectedSurfaces.stream().map(s -> s.toDto()).collect(Collectors.toList()));
        this.renderFromProject();
    }

    public void surfaceHole() {
        List<SurfaceUI> selectedSurfaces = this.selectionManager.getSelectedSurfaces();

        for (SurfaceUI surface: selectedSurfaces) {
            surface.setHole(true);
            domainController.updateSurface(surface.toDto());
            surface.hideTiles();
        }
        hideRectangleInfo();
        renderFromProject();
    }

    public void undo() {
        this.domainController.undo();
        renderFromProject();
    }

    public void redo() {
        this.domainController.redo();
        renderFromProject();
    }
}
