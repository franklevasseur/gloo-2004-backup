package sample;

import Domain.HoleStatus;
import application.*;
import gui.*;

import gui.sidepanel.SidePanelUI;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.*;

import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Scale;
import javafx.stage.FileChooser;
import utils.*;
import utils.imperial.ImperialFractionHelper;

import java.io.File;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

public class UiController implements Initializable {

    private ObservableList<String> possibleColor = FXCollections.observableArrayList("", "BLACK", "WHITE", "YELLOW", "GREEN", "BLUE", "RED", "VIOLET");

    public Pane pane;
    public Pane drawingSection;

    //Snap grid
    public TextField resizeSG;
    public Label snapgridLabel;
    public Button snapGridbutton;

    // inspection
    public TextField minInspectionLengthTextField;
    public Button inspectButton;
    public TextArea inspectionArea;
    private Double minInspectionLength;

    // create material properties
    public TextField materialNameInputBox;
    public TextField tilePerBoxInputBox;
    public TextField boxPriceInputBox;
    public TextField tileHeightMaterialInputBox;
    public TextField tileWidthMaterialInputBox;
    public ChoiceBox<String> materialColorChoiceBox;

    // edit material
    public TextField mNewHeightInputBox;
    public TextField mNewLenghtInputBox;
    public TextField mNewTilePerBoxInput;
    public TextField mNewPricePerBoxInputBox;
    public ChoiceBox<String> editTileMaterialChoiceBox;
    public ChoiceBox<String> mNewColorInputBox;

    // accounting
    public TableView<MaterialUI> materialTableView;
    public TableColumn<MaterialUI, String> materialNameColumn;
    public TableColumn<MaterialUI, String> materialNumberOfBoxInputColumn;
    public TableColumn<MaterialUI, String> materialTilePerBoxColumn;
    public TableColumn<MaterialUI, String> materialColorColumn;
    public TableColumn<MaterialUI, String> materialPricePerBoxColumn;
    public TableColumn<MaterialUI, String> materialTotalPriceColumn;
    public TableColumn<MaterialUI, String> nbTileColumn;

    // surface properties inputs
    public TextField tileHeightInputbox;
    public TextField tileWidthInputbox;
    public TextField sealWidthInputBox;
    public TextField surfaceHeightInputBox;
    public TextField surfaceWidthInputBox;
    public TextField masterTileX;
    public TextField masterTileY;
    public TextField tileAngleInputBox;
    public TextField tileShiftingInputBox;
    public TextField surfacePositionXInputBox;
    public TextField surfacePositionYInputBox;
    public ChoiceBox<String> surfaceColorChoiceBox;
    public ChoiceBox<String> sealColorChoiceBox;
    public ChoiceBox<String> tilePatternInputBox;
    public ChoiceBox<String> tileMaterialChoiceBox;

    // distance surfaces
    public Label distanceBetweenSurfacesLabelText;
    public Label distanceBetweenSurfacesLabel;

    // bottom left label for tile under cursor info
    public Label tileInfo;

    // top bar buttons
    public CheckBox snapGridCheckBox;
    public CheckBox mooveTilesCheckBox;

    public CheckBox imperialCheckBox;
    public CheckBox metricCheckBox;

    public Button undoButton;
    public Button redoButton;

    public Button fillTilesButton;
    public boolean stateCurrentlyFilling = true;

    public Button fusionButton;
    public boolean stateCurrentlyFusionning = true;

    // services
    private List<SurfaceUI> allSurfaces = new ArrayList<>();
    private SelectionManager selectionManager;
    private ZoomManager zoomManager = new ZoomManager();
    private SidePanelUI sidePanel;
    private SnapGridUI snapGridUI;
    private Controller domainController = Controller.getInstance();
    private ImperialFractionHelper numberUtils = new ImperialFractionHelper();

    // state variables to make coherent state machine
    private boolean stateCurrentlyCreatingRectangularSurface = false;
    private boolean stateCurrentlyCreatingIrregularSurface = false;
    private boolean stateTopLeftCornerCreated = false;
    private boolean stateEnableZooming = false;
    private boolean metricDisplay = true;
    private Point firstClickCoord;
    private Rectangle rectangleSurfaceCreationIndicator;
    private List<AttachmentPointUI> irregularSurfaceSummits = new ArrayList<>();

    // others
    Alert alert = new Alert(Alert.AlertType.WARNING);

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("Initialization...");

        // Invisible Pane object that contains all other shapes
        // Needed to be invisible so zooming out of it would not expose its edge
        // Make it look like its infinite in size
        drawingSection.setPrefHeight(1);
        drawingSection.setPrefWidth(1);
        materialColorChoiceBox.setItems(possibleColor);
        mNewColorInputBox.setItems(possibleColor);
        sealColorChoiceBox.setItems(possibleColor);
        surfaceColorChoiceBox.setItems(possibleColor);
        tilePatternInputBox.setItems(PatternHelperUI.getPossibleTilePatterns());

        materialNameColumn.setCellValueFactory(new PropertyValueFactory<MaterialUI, String>("name"));
        materialNumberOfBoxInputColumn.setCellValueFactory(new PropertyValueFactory<MaterialUI, String>("numberOfBoxes"));
        materialTilePerBoxColumn.setCellValueFactory(new PropertyValueFactory<MaterialUI, String>("tilePerBox"));
        materialColorColumn.setCellValueFactory(new PropertyValueFactory<MaterialUI, String>("color"));
        materialPricePerBoxColumn.setCellValueFactory(new PropertyValueFactory<MaterialUI, String>("pricePerBoxe"));
        materialTotalPriceColumn.setCellValueFactory(new PropertyValueFactory<MaterialUI, String>("totalPrice"));
        nbTileColumn.setCellValueFactory(new PropertyValueFactory<MaterialUI, String>("numberOfTiles"));

        tileWidthInputbox.setDisable(true);
        tileHeightInputbox.setDisable(true);

        this.snapGridUI = new SnapGridUI(this.drawingSection);
        this.selectionManager = new SelectionManager(this::handleSelection);

        domainController.setUndoRedoListener(this::listenForUndoRedo);

        this.undoButton.setDisable(!this.domainController.undoAvailable());
        this.redoButton.setDisable(!this.domainController.redoAvailable());

        this.pane.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
            if (this.stateCurrentlyCreatingRectangularSurface || this.stateCurrentlyCreatingIrregularSurface) {
                onPaneClicked(e);
                e.consume();
            }
        });

        inspectionArea.setDisable(true);
        inspectionArea.setStyle("-fx-text-fill: #ff0000; -fx-opacity: 1.0;");
        inspectButton.setDisable(true);

        NumberFormat format = NumberFormat.getInstance(Locale.FRANCE);
        minInspectionLengthTextField.textProperty().addListener((observableValue, oldString, newString) -> {

            boolean parseSucess = true;
            try {
                CharSequence minInspectionLengthInput = this.minInspectionLengthTextField.getCharacters();
                minInspectionLength = minInspectionLengthInput.toString().equals("") ? null : format.parse(minInspectionLengthInput.toString()).doubleValue();

            } catch (ParseException e) {
                parseSucess = false;
            }

            if (parseSucess) {
                inspectButton.setDisable(false);
            }
        });

        materialNameInputBox.textProperty().addListener((observableValue, oldString, newString) -> {

            if (!createMaterialListener()) {
                inspectButton.setDisable(false);
            } else {
                inspectButton.setDisable(true);
            }
        });

        imperialCheckBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean aBoolean, Boolean t1) {
                metricCheckBox.setSelected(!t1);
                if (t1) {
                    metricDisplay = false;
                    sidePanel.updateSelection(selectionManager.getSelectedSurfaces(), metricDisplay);
                }
            }
        });

        metricCheckBox.setSelected(metricDisplay);
        metricCheckBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean aBoolean, Boolean t1) {
                imperialCheckBox.setSelected(!t1);

                if (t1) {
                    metricDisplay = true;
                    sidePanel.updateSelection(selectionManager.getSelectedSurfaces(), metricDisplay);
                }

            }
        });

        alert.setTitle("WARNING");

        sidePanel = new SidePanelUI(zoomManager,
                alert,
                tileHeightInputbox,
                tileWidthInputbox,
                sealWidthInputBox,
                surfaceHeightInputBox,
                surfaceWidthInputBox,
                masterTileX,
                masterTileY,
                tileAngleInputBox,
                tileShiftingInputBox,
                surfacePositionXInputBox,
                surfacePositionYInputBox,
                sealColorChoiceBox,
                tilePatternInputBox,
                tileMaterialChoiceBox,
                surfaceColorChoiceBox,
                materialNameInputBox,
                tilePerBoxInputBox,
                boxPriceInputBox,
                tileHeightMaterialInputBox,
                tileWidthMaterialInputBox,
                materialColorChoiceBox,
                materialTableView,
                mNewHeightInputBox,
                mNewLenghtInputBox,
                mNewTilePerBoxInput,
                mNewPricePerBoxInputBox,
                editTileMaterialChoiceBox,
                mNewColorInputBox,
                distanceBetweenSurfacesLabelText,
                distanceBetweenSurfacesLabel,
                resizeSG,
                snapgridLabel,
                snapGridbutton,
                snapGridUI);

        renderFromProject();
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
        if (e.getCode() == KeyCode.DELETE) {
            removeSelectedSurfaces();
        }
        if (e.getCode() == KeyCode.CONTROL) {
            selectionManager.allowMultipleSelection();
            stateEnableZooming = true;
        }
        if (e.getCode() == KeyCode.ALT) {
            resetZoom();
        }
    }

    public void onMouseMoved(MouseEvent e) {
        if (stateTopLeftCornerCreated) {
            drawingSection.getChildren().remove(rectangleSurfaceCreationIndicator);

            Point mouseCoord = this.getPointInReferenceToOrigin(new Point(e.getX(), e.getY()));
            Point topLeft = RectangleHelper.getTopLeft(firstClickCoord, mouseCoord);

            double width = Math.abs(mouseCoord.x - firstClickCoord.x);
            double heigth = Math.abs(mouseCoord.y - firstClickCoord.y);
            rectangleSurfaceCreationIndicator = new Rectangle(topLeft.x, topLeft.y, width, heigth);
            rectangleSurfaceCreationIndicator.setFill(javafx.scene.paint.Color.GRAY);
            drawingSection.getChildren().add(rectangleSurfaceCreationIndicator);
        }
    }

    public void handleKeyReleased(KeyEvent e) {
        if (e.getCode() == KeyCode.CONTROL) {
            selectionManager.disableMultipleSelection();
            stateEnableZooming = false;
        }
    }

    public void onPaneClicked(MouseEvent e) {

        Point clickCoord = this.getPointInReferenceToOrigin(new Point(e.getX(), e.getY()));

//        System.out.println(String.format("click : (%f, %f)", zoomManager.pixelsToMeters(clickCoord.x), zoomManager.pixelsToMeters(clickCoord.y)));
        if (stateCurrentlyCreatingRectangularSurface) {
            handleRectangularSurfaceCreation(clickCoord);
        } else if (stateCurrentlyCreatingIrregularSurface) {
            handleIrregularSurfaceCreation(clickCoord);
        }

        stateCurrentlyFilling = true;
        fillTilesButton.setText("Fill tiles");

        stateCurrentlyFusionning = true;
        fusionButton.setText("Fusion surfaces");

        selectionManager.unselectAll();
        sidePanel.hideInfo(metricDisplay);
    }

    private void handleRectangularSurfaceCreation(Point click) {

        Point clickCoord = click;
        if (this.snapGridUI.isVisible()) {
            clickCoord = this.snapGridUI.getNearestGridPoint(clickCoord);
        }

        if (!stateTopLeftCornerCreated) {
            firstClickCoord = new Point(clickCoord.x, clickCoord.y);
            stateTopLeftCornerCreated = true;
            return;
        }

        stateCurrentlyCreatingRectangularSurface = false;
        stateTopLeftCornerCreated = false;

        if (!clickCoord.isInRange(firstClickCoord, 5)) {
            createRectangularSurfaceHere(new Point(firstClickCoord.x, firstClickCoord.y), new Point(clickCoord.x, clickCoord.y));
        }

        drawingSection.getChildren().remove(rectangleSurfaceCreationIndicator);
        pane.setCursor(Cursor.DEFAULT);
        this.renderFromProject();

        selectionManager.unselectAll();
        firstClickCoord = null;

        sidePanel.hideInfo(metricDisplay);
    }

    private void handleIrregularSurfaceCreation(Point clickCoord) {
        Point pointToAdd = clickCoord;
        if (this.snapGridUI.isVisible()) {
            pointToAdd = this.snapGridUI.getNearestGridPoint(clickCoord);
        }

        boolean isFirstPoint = irregularSurfaceSummits.size() < 1;
        Cursor cursor = isFirstPoint ? Cursor.HAND : null;

        AttachmentPointUI point = new AttachmentPointUI(pointToAdd, null, null, cursor);
        irregularSurfaceSummits.add(point);
        this.drawingSection.getChildren().add(point.getNode());

        if (isFirstPoint) {
            return;
        }

        AttachmentPointUI firstPoint = irregularSurfaceSummits.get(0);
        double samePositionTolerance = 10;
        if (clickCoord.isInRange(firstPoint.getPixelCoords(), samePositionTolerance)) {
            if (irregularSurfaceSummits.size() > 2) {
                // remove last point added
                irregularSurfaceSummits.remove(irregularSurfaceSummits.size() - 1);
                this.drawingSection.getChildren().remove(this.drawingSection.getChildren().size() - 1);
                createIrregularSurfaceHere(irregularSurfaceSummits.stream().map(s -> s.getPixelCoords()).collect(Collectors.toList()));
            }

            stateCurrentlyCreatingIrregularSurface = false;
            pane.setCursor(Cursor.DEFAULT);
            this.drawingSection.getChildren().removeAll(irregularSurfaceSummits.stream().map(s -> s.getNode()).collect(Collectors.toList()));
            irregularSurfaceSummits.clear();
            this.renderFromProject();
        }

        selectionManager.unselectAll();
        sidePanel.hideInfo(metricDisplay);
    }

    public Void handleSelection(Void nothing) {
        List<SurfaceUI> selectedSurfaces = selectionManager.getSelectedSurfaces();

        if (selectedSurfaces.stream().allMatch(s -> s.toDto().isHole == HoleStatus.FILLED)) {
            stateCurrentlyFilling = false;
            fillTilesButton.setText("Unfill tiles");
        } else {
            stateCurrentlyFilling = true;
            fillTilesButton.setText("Fill tiles");
        }

        if (selectedSurfaces.stream().allMatch(s -> s.toDto().isFusionned)) {
            stateCurrentlyFusionning = false;
            fusionButton.setText("UnFusion surfaces");
        } else {
            stateCurrentlyFusionning = true;
            fusionButton.setText("Fusion surfaces");
        }

        if (selectedSurfaces.stream().anyMatch(s -> !s.toDto().isRectangular)) {
            surfaceHeightInputBox.setDisable(true);
        } else {
            surfaceHeightInputBox.setDisable(false);
        }

        sidePanel.updateSelection(selectedSurfaces, metricDisplay);

        return null;
    }

    public Void listenForUndoRedo(Void nothing) {
        this.undoButton.setDisable(!this.domainController.undoAvailable());
        this.redoButton.setDisable(!this.domainController.redoAvailable());
        return nothing;
    }

    public void editSurface() {
        sidePanel.editSurface(selectionManager.getSelectedSurfaces(), metricDisplay);
        renderFromProject();
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

    public void onCreateRectangularSurfaceSelected() {
        if (!stateCurrentlyCreatingRectangularSurface) {
            stateCurrentlyCreatingRectangularSurface = true;
            pane.setCursor(Cursor.CROSSHAIR);
        }
    }

    public void onCreateIrregularSurfaceSelected() {
        if (!stateCurrentlyCreatingIrregularSurface) {
            stateCurrentlyCreatingIrregularSurface = true;
            pane.setCursor(Cursor.CROSSHAIR);
        }
    }

    public void snapGridToggle() {
        if (snapGridCheckBox.isSelected()) {
            this.snapGridUI.setVisibility(true);
            this.snapGridUI.renderForViewBox(this.getViewBoxSummits());
            sidePanel.displaySnapGrid(metricDisplay);
        } else {
            this.snapGridUI.setVisibility(false);
            this.snapGridUI.removeGrid();
            sidePanel.hideSnapGrid();
        }
    }

    public void moveTilesToggle() {
        if (mooveTilesCheckBox.isSelected()) {
            this.allSurfaces.forEach(s -> s.setCurrentlyMovingTiles(true));
            return;
        }
        this.allSurfaces.forEach(s -> s.setCurrentlyMovingTiles(false));
    }

    private List<Point> getViewBoxSummits() {
        List<Point> viewBorders = new ArrayList<>();

        Point zeroZero = new Point(0, 0);
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

    public void toggleFill() {
        if (stateCurrentlyFilling) {
            fillSelectedSurfaceWithTiles();
        } else {
            unfillTiles();
        }
    }

    public void fillSelectedSurfaceWithTiles() {
        List<SurfaceUI> selectedSurfaces = this.selectionManager.getSelectedSurfaces();

        for (SurfaceUI surface : selectedSurfaces) {
            surface.forceFill();
        }

        this.renderFromProject();
    }

    public void unfillTiles() {
        List<SurfaceUI> selectedSurfaces = this.selectionManager.getSelectedSurfaces();

        for (SurfaceUI surface : selectedSurfaces) {
            surface.setHole(HoleStatus.NONE);
            domainController.updateSurface(surface.toDto());
            surface.hideTiles();
            surface.setHole(HoleStatus.NONE);

            if (surface.toDto().isFusionned) {
                FusionedSurfaceUI fs = (FusionedSurfaceUI) surface;
                fs.getInnerSurfaces().forEach(s -> s.setHole(HoleStatus.NONE));
            }
        }

        sidePanel.hideInfo(metricDisplay);
        this.renderFromProject();
    }

    public void setHole() {
        List<SurfaceUI> selectedSurfaces = this.selectionManager.getSelectedSurfaces();

        for (SurfaceUI surface : selectedSurfaces) {
            surface.setHole(HoleStatus.HOLE);
            domainController.updateSurface(surface.toDto());
        }

        this.renderFromProject();
    }

    private void removeSelectedSurfaces() {
        List<SurfaceUI> selectedSurfaces = selectionManager.getSelectedSurfaces();
        drawingSection.getChildren().removeIf(selectedSurfaces.stream().map(s -> s.getNode()).collect(Collectors.toList())::contains);
        selectedSurfaces.forEach(SurfaceUI::delete);
        allSurfaces.removeIf(selectedSurfaces::contains);
        selectionManager.unselectAll();
    }

    private void createRectangularSurfaceHere(Point location1, Point location2) {

        Point topLeft = RectangleHelper.getTopLeft(location1, location2);

        double widthPixels = Math.abs(location2.x - location1.x);
        double heightPixels = Math.abs(location2.y - location1.y);

        Point actualPoint = this.snapGridUI.isVisible() ? this.snapGridUI.getNearestGridPoint(topLeft) : topLeft;

        double x = zoomManager.pixelsToMeters(actualPoint.x);
        double y = zoomManager.pixelsToMeters(actualPoint.y);
        double width = zoomManager.pixelsToMeters(widthPixels);
        double height = zoomManager.pixelsToMeters(heightPixels);

        SurfaceDto surface = new SurfaceDto();
        surface.id = new Id();
        surface.isHole = HoleStatus.NONE;
        surface.isRectangular = true;
        surface.summits = RectangleHelper.rectangleInfoToSummits(new Point(x, y), width, height);

        domainController.createSurface(surface);
    }

    private void createIrregularSurfaceHere(List<Point> pixelsSummits) {

        List<Point> metersSummits = pixelsSummits.stream().map(px -> zoomManager.pixelsToMeters(px)).collect(Collectors.toList());

        SurfaceDto surface = new SurfaceDto();
        surface.id = new Id();
        surface.isHole = HoleStatus.NONE;
        surface.isRectangular = false;
        surface.isFusionned = false;
        surface.summits = metersSummits;

        domainController.createSurface(surface);
    }

    private void renderFromProject() {
        this.clearDrawings();

        this.listenForUndoRedo(null);

        ProjectDto project = this.domainController.getProject();
        if (project.surfaces != null) {
            for (SurfaceDto surface : project.surfaces) {
                this.displaySurface(surface);
            }
        }

        allSurfaces.forEach(s -> s.setCurrentlyMovingTiles(this.mooveTilesCheckBox.isSelected()));

        if (project.materials != null) {
            this.tileMaterialChoiceBox.getItems().clear();
            editTileMaterialChoiceBox.getItems().clear();
            for (MaterialDto mDto : project.materials) {
                tileMaterialChoiceBox.getItems().add(mDto.name);
                editTileMaterialChoiceBox.getItems().add(mDto.name);
            }

            sidePanel.updateSelection(selectionManager.getSelectedSurfaces(), metricDisplay);
        }
    }

    private void clearDrawings() {
        allSurfaces.forEach(SurfaceUI::hide);
        drawingSection.getChildren().removeIf(allSurfaces.stream().map(s -> s.getNode()).collect(Collectors.toList())::contains);
        selectionManager.unselectAll();
        allSurfaces.clear();
        snapGridUI.renderForViewBox(this.getViewBoxSummits());
        materialTableView.getItems().clear();
    }

    private void displaySurface(SurfaceDto surfaceDto) {
        if (surfaceDto.isFusionned) {
            FusionedSurfaceUI surfaceUi = new FusionedSurfaceUI(surfaceDto, zoomManager, selectionManager, snapGridUI, tileInfo);
            this.drawingSection.getChildren().add(surfaceUi.getNode());
            this.allSurfaces.add(surfaceUi);
            return;
        }

        if (surfaceDto.isRectangular) {
            RectangleSurfaceUI surfaceUi = new RectangleSurfaceUI(surfaceDto,
                    zoomManager,
                    selectionManager,
                    snapGridUI,
                    this.tileInfo);
            this.drawingSection.getChildren().add(surfaceUi.getNode());
            this.allSurfaces.add(surfaceUi);
            return;
        }

        IrregularSurfaceUI irregularSurfaceUI = new IrregularSurfaceUI(surfaceDto,
                zoomManager,
                selectionManager,
                snapGridUI,
                this.tileInfo);
        this.drawingSection.getChildren().add(irregularSurfaceUI.getNode());
        this.allSurfaces.add(irregularSurfaceUI);
    }

    public void fusionToggle() {
        sidePanel.hideInfo(metricDisplay);

        if (stateCurrentlyFusionning) {
            fusionSurfaces();
            return;
        }
        unfusionSurfaces();
    }

    public void fusionSurfaces() {
        if (this.selectionManager.getSelectedSurfaces().size() <= 1) {
            return;
        }

        List<SurfaceUI> selectedSurfaces = this.selectionManager.getSelectedSurfaces();
        this.domainController.fusionSurfaces(selectedSurfaces.stream().map(s -> s.toDto()).collect(Collectors.toList()));
        this.renderFromProject();
    }

    public void unfusionSurfaces() {
        if (this.selectionManager.getSelectedSurfaces().size() != 1) {
            return;
        }

        SurfaceUI selectedSurfaces = this.selectionManager.getSelectedSurfaces().get(0);
        this.domainController.unFusionSurface(selectedSurfaces.toDto());
        this.renderFromProject();
    }

    public void createNewMaterial() {
        sidePanel.createNewMaterial(metricDisplay);
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

    public void centerSurfacesVertically() {
        if (this.selectionManager.getSelectedSurfaces().size() <= 1) {
            return;
        }

        List<SurfaceUI> selectedSurfaces = this.selectionManager.getSelectedSurfaces();
        SurfaceUI mainSurface = selectedSurfaces.get(0);

        AbstractShape firstSurface = new AbstractShape(mainSurface.toDto().summits);
        Point firstTopLeft = ShapeHelper.getTopLeftCorner(firstSurface);
        double firstX = firstTopLeft.x;
        double centerX = ShapeHelper.getWidth(firstSurface) / 2;

        for (SurfaceUI s : selectedSurfaces) {
            if (s == mainSurface) {
                continue;
            }
            AbstractShape surface = new AbstractShape(s.toDto().summits);
            Point surfaceTopLeft = ShapeHelper.getTopLeftCorner(surface);
            double surfaceHalfWidth = ShapeHelper.getWidth(surface) / 2;
            double surfaceY = surfaceTopLeft.y;
            s.setPosition(new Point((firstX + centerX) - surfaceHalfWidth, surfaceY));
            if (s.toDto().isHole == HoleStatus.FILLED) {
                domainController.updateAndRefill(s.toDto(), s.getMasterTile(), s.getPattern(), s.getSealsInfo(), s.getTileAngle(), s.getTileShifting());
            } else {
                domainController.updateSurface(s.toDto());
            }
        }
        this.renderFromProject();
    }

    public void centerSurfacesHorizontally() {
        if (this.selectionManager.getSelectedSurfaces().size() <= 1) {
            return;
        }

        List<SurfaceUI> selectedSurfaces = this.selectionManager.getSelectedSurfaces();
        SurfaceUI mainSurface = selectedSurfaces.get(0);

        AbstractShape firstSurface = new AbstractShape(mainSurface.toDto().summits);
        Point firstTopLeft = ShapeHelper.getTopLeftCorner(firstSurface);
        double firstY = firstTopLeft.y;
        double centerY = ShapeHelper.getHeight(firstSurface) / 2;

        for (SurfaceUI s : selectedSurfaces) {
            if (s == mainSurface) {
                continue;
            }

            AbstractShape surface = new AbstractShape(s.toDto().summits);
            Point surfaceTopLeft = ShapeHelper.getTopLeftCorner(surface);
            double surfaceX = surfaceTopLeft.x;
            double halfHeight = ShapeHelper.getHeight(surface) / 2;
            s.setPosition(new Point(surfaceX, (firstY + centerY) - halfHeight));
            if (s.toDto().isHole == HoleStatus.FILLED) {
                domainController.updateAndRefill(s.toDto(), s.getMasterTile(), s.getPattern(), s.getSealsInfo(), s.getTileAngle(), s.getTileShifting());
            } else {
                domainController.updateSurface(s.toDto());
            }
        }
        this.renderFromProject();
    }

    public void alignLeftSurfaces() {
        if (this.selectionManager.getSelectedSurfaces().size() <= 1) {
            return;
        }
        List<SurfaceUI> selectedSurfaces = this.selectionManager.getSelectedSurfaces();
        SurfaceUI mainSurface = selectedSurfaces.get(0);

        AbstractShape firstSurface = new AbstractShape(mainSurface.toDto().summits);
        Point topLeft = ShapeHelper.getTopLeftCorner(firstSurface);
        double firstX = topLeft.x;

        for (SurfaceUI s : selectedSurfaces) {
            if (s == mainSurface) {
                continue;
            }
            AbstractShape surface = new AbstractShape(s.toDto().summits);
            Point surfaceTopLeft = ShapeHelper.getTopLeftCorner(surface);
            double surfaceY = surfaceTopLeft.y;
            s.setPosition(new Point(firstX, surfaceY));

            if (s.toDto().isHole == HoleStatus.FILLED) {
                domainController.updateAndRefill(s.toDto(), s.getMasterTile(), s.getPattern(), s.getSealsInfo(), s.getTileAngle(), s.getTileShifting());
            } else {
                domainController.updateSurface(s.toDto());
            }
        }
        this.renderFromProject();
    }

    public void alignRightSurfaces() {
        if (this.selectionManager.getSelectedSurfaces().size() <= 1) {
            return;
        }
        List<SurfaceUI> selectedSurfaces = this.selectionManager.getSelectedSurfaces();
        SurfaceUI mainSurface = selectedSurfaces.get(0);

        AbstractShape firstSurface = new AbstractShape(mainSurface.toDto().summits);
        Point topLeft = ShapeHelper.getTopLeftCorner(firstSurface);
        double firstX = topLeft.x;
        double firstWidth = ShapeHelper.getWidth(firstSurface);

        for (SurfaceUI s : selectedSurfaces) {
            if (s == mainSurface) {
                continue;
            }
            AbstractShape surface = new AbstractShape(s.toDto().summits);
            Point surfaceTopLeft = ShapeHelper.getTopLeftCorner(surface);
            double surfaceY = surfaceTopLeft.y;
            double surfaceWidth = ShapeHelper.getWidth(surface);
            s.setPosition(new Point(firstX + firstWidth - surfaceWidth, surfaceY));

            if (s.toDto().isHole == HoleStatus.FILLED) {
                domainController.updateAndRefill(s.toDto(), s.getMasterTile(), s.getPattern(), s.getSealsInfo(), s.getTileAngle(), s.getTileShifting());
            } else {
                domainController.updateSurface(s.toDto());
            }
        }
        this.renderFromProject();
    }

    public void alignTopSurfaces() {
        if (this.selectionManager.getSelectedSurfaces().size() <= 1) {
            return;
        }
        List<SurfaceUI> selectedSurfaces = this.selectionManager.getSelectedSurfaces();
        SurfaceUI mainSurface = selectedSurfaces.get(0);

        AbstractShape firstSurface = new AbstractShape(mainSurface.toDto().summits);
        Point topLeft = ShapeHelper.getTopLeftCorner(firstSurface);
        double firstY = topLeft.y;

        for (SurfaceUI s : selectedSurfaces) {
            if (s == mainSurface) {
                continue;
            }
            AbstractShape surface = new AbstractShape(s.toDto().summits);
            Point surfaceTopLeft = ShapeHelper.getTopLeftCorner(surface);
            double surfaceX = surfaceTopLeft.x;
            s.setPosition(new Point(surfaceX, firstY));

            if (s.toDto().isHole == HoleStatus.FILLED) {
                domainController.updateAndRefill(s.toDto(), s.getMasterTile(), s.getPattern(), s.getSealsInfo(), s.getTileAngle(), s.getTileShifting());
            } else {
                domainController.updateSurface(s.toDto());
            }
        }
        this.renderFromProject();
    }

    public void alignBottomSurfaces() {
        if (this.selectionManager.getSelectedSurfaces().size() <= 0) {
            return;
        }
        List<SurfaceUI> selectedSurfaces = this.selectionManager.getSelectedSurfaces();
        SurfaceUI mainSurface = selectedSurfaces.get(0);

        AbstractShape firstSurface = new AbstractShape(mainSurface.toDto().summits);
        Point topLeft = ShapeHelper.getTopLeftCorner(firstSurface);
        double firstY = topLeft.y;
        double firstHeight = ShapeHelper.getHeight(firstSurface);

        for (SurfaceUI s : selectedSurfaces) {
            if (s == mainSurface) {
                continue;
            }
            AbstractShape surface = new AbstractShape(s.toDto().summits);
            Point surfaceTopLeft = ShapeHelper.getTopLeftCorner(surface);
            double surfaceX = surfaceTopLeft.x;
            double surfaceHeight = ShapeHelper.getHeight(surface);
            s.setPosition(new Point(surfaceX, firstY + firstHeight - surfaceHeight));

            if (s.toDto().isHole == HoleStatus.FILLED) {
                domainController.updateAndRefill(s.toDto(), s.getMasterTile(), s.getPattern(), s.getSealsInfo(), s.getTileAngle(), s.getTileShifting());
            } else {
                domainController.updateSurface(s.toDto());
            }
        }
        this.renderFromProject();
    }

    public void stickSurfacesVertically() {
        if (this.selectionManager.getSelectedSurfaces().size() <= 1) {
            return;
        }

        List<SurfaceUI> selectedSurfaces = this.selectionManager.getSelectedSurfaces();
        SurfaceUI mainSurface = selectedSurfaces.get(0);

        AbstractShape firstSurface = new AbstractShape(mainSurface.toDto().summits);
        Point firstTopLeft = ShapeHelper.getTopLeftCorner(firstSurface);
        double firstY = firstTopLeft.y;
        double firstHeight = ShapeHelper.getHeight(firstSurface);

        for (SurfaceUI s : selectedSurfaces) {
            if (s == mainSurface) {
                continue;
            }
            AbstractShape surface = new AbstractShape(s.toDto().summits);
            Point surfaceTopLeft = ShapeHelper.getTopLeftCorner(surface);
            double surfaceX = surfaceTopLeft.x;
            double surfaceY = surfaceTopLeft.y;
            double surfaceHeight = ShapeHelper.getHeight(surface);

            if (surfaceY > firstY) {
                s.setPosition(new Point(surfaceX, firstY + firstHeight));
            } else {
                s.setPosition(new Point(surfaceX, firstY - surfaceHeight));
            }

            if (s.toDto().isHole == HoleStatus.FILLED) {
                domainController.updateAndRefill(s.toDto(), s.getMasterTile(), s.getPattern(), s.getSealsInfo(), s.getTileAngle(), s.getTileShifting());
            } else {
                domainController.updateSurface(s.toDto());
            }
        }
        this.renderFromProject();
    }

    public void stickSurfacesHorizontally() {
        if (this.selectionManager.getSelectedSurfaces().size() <= 1) {
            return;
        }
        List<SurfaceUI> selectedSurfaces = this.selectionManager.getSelectedSurfaces();
        SurfaceUI mainSurface = selectedSurfaces.get(0);

        AbstractShape firstSurface = new AbstractShape(mainSurface.toDto().summits);
        Point firstTopLeft = ShapeHelper.getTopLeftCorner(firstSurface);
        double firstX = firstTopLeft.x;
        double firstWidth = ShapeHelper.getWidth(firstSurface);

        for (SurfaceUI s : selectedSurfaces) {
            if (s == mainSurface) {
                continue;
            }
            AbstractShape surface = new AbstractShape(s.toDto().summits);
            Point surfaceTopLeft = ShapeHelper.getTopLeftCorner(surface);
            double surfaceX = surfaceTopLeft.x;
            double surfaceY = surfaceTopLeft.y;
            double surfaceWidth = ShapeHelper.getWidth(surface);
            if (surfaceX > firstX) {
                s.setPosition(new Point(firstX + firstWidth, surfaceY));
            } else {
                s.setPosition(new Point(firstX - surfaceWidth, surfaceY));
            }

            if (s.toDto().isHole == HoleStatus.FILLED) {
                domainController.updateAndRefill(s.toDto(), s.getMasterTile(), s.getPattern(), s.getSealsInfo(), s.getTileAngle(), s.getTileShifting());
            } else {
                domainController.updateSurface(s.toDto());
            }
        }
        this.renderFromProject();
    }

    public void inspect() {
        String unit = "m";
        double inspectValue = minInspectionLength;
        if (!metricDisplay) {
            inspectValue = zoomManager.inchToMeters(minInspectionLength);
            unit = "in";
        }
        String inspectionResult = domainController.inspectProject(inspectValue, inspectValue);
        inspectionArea.setText(String.format("Inspection result for min lenght = %.2f %s : \n\n%s", minInspectionLength, unit, inspectionResult));
        this.renderFromProject();
    }

    public void SaveProject() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sauvegarder Plan");
        fileChooser.setInitialDirectory(new File("./"));
        fileChooser.setInitialFileName("project.bin");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Java Serialized object", "*.bin"));
        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            domainController.saveProject(file.getPath());
        }

    }

    public void LoadProject() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load Plan");
        fileChooser.setInitialDirectory(new File("./"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Java Serialized object", "*.bin"));
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            domainController.loadProject(file.getPath());
        }

        this.renderFromProject();
    }

    public void newProject() {
        domainController.newProject();
        renderFromProject();
    }

    private boolean createMaterialListener() {
        boolean parseSucess = true;
        if (materialNameInputBox.getText() == null) {
            parseSucess = false;
        }
        return parseSucess;
    }

    public void snapGridApply() {
        sidePanel.applySnapGrid(metricDisplay);
    }

    public void editMaterialButton() {
        sidePanel.updateMaterial(metricDisplay);
        renderFromProject();
    }

    private void displayMaterialInfo() {
        NumberFormat formatter = new DecimalFormat("#0.000");

        String materialName = editTileMaterialChoiceBox.getValue();
        Optional<MaterialDto> optionalMaterial = domainController.getMaterialByName(materialName);

        if (optionalMaterial.isPresent()) {
            MaterialDto material = optionalMaterial.get();
            mNewHeightInputBox.setText(formatter.format(material.tileTypeHeight));
            mNewLenghtInputBox.setText(formatter.format(material.tileTypeWidth));
            mNewTilePerBoxInput.setText(formatter.format(material.nbTilePerBox));
            mNewPricePerBoxInputBox.setText(formatter.format(material.costPerBox));
            mNewColorInputBox.setValue(ColorHelper.utilsColorToString(material.color));
        }
    }
}
