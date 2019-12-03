package sample;

import Domain.Accounting;
import Domain.HoleStatus;
import Domain.MaterialType;
import Domain.PatternType;
import application.*;
import gui.*;

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

import java.io.File;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

public class UiController implements Initializable {

    public Pane pane;
    public Pane drawingSection;

    //Material propreties
    private ObservableList<String> possibleColor = FXCollections.observableArrayList("", "BLACK","WHITE","YELLOW","GREEN","BLUE","RED","VIOLET");

    public TextField materialNameInputBox;
    public TextField tilePerBoxInputBox;
    public TextField boxPriceInputBox;
    public TextField tileHeightMaterialInputBox;
    public TextField tileWidthMaterialInputBox;

    public ChoiceBox<String> materialColorChoiceBox;

    public TableView<MaterialUI> materialTableView;
    public TableColumn<MaterialUI,String> materialNameColumn;
    public TableColumn<MaterialUI,String> materialNumberOfBoxInputColumn;
    public TableColumn<MaterialUI,String> materialTilePerBoxColumn;
    public TableColumn<MaterialUI,String> materialColorColumn;
    public TableColumn<MaterialUI,String> materialPricePerBoxColumn;
    public TableColumn<MaterialUI,String> materialTotalPriceColumn;
    public TableColumn<MaterialUI,String> nbTileColumn;

    // surface properties inputs
    public TextField tileHeightInputbox;
    public TextField tileWidthInputbox;
    public TextField sealWidthInputBox;
    public TextField surfaceHeightInputBox;
    public TextField surfaceWidthInputBox;
    public TextField masterTileX;
    public TextField masterTileY;
    public TextField surfacePositionXInputBox;
    public TextField surfacePositionYInputBox;

    public Button fillTilesButton;
    public boolean stateCurrentlyFilling = true;

    public TextField materialColorDisplay;

    public Button fusionButton;
    public boolean stateCurrentlyFusionning = true;

    public Label tileInfo;

    public ChoiceBox<String> sealColorChoiceBox;
    public ChoiceBox<String> tilePatternInputBox;
    public ChoiceBox<String> tileMaterialChoiceBox;

    public CheckBox snapGridCheckBox;

    private List<SurfaceUI> allSurfaces = new ArrayList<>();
    private SelectionManager selectionManager;
    private ZoomManager zoomManager = new ZoomManager();
    private SnapGridUI snapGridUI;

    public Button undoButton;
    public Button redoButton;

    // state variables to make coherent state machine
    private boolean stateCurrentlyCreatingRectangularSurface = false;
    private boolean stateCurrentlyCreatingIrregularSurface = false;
    private boolean stateTopLeftCornerCreated = false;
    private boolean stateEnableZooming = false;

    private Point firstClickCoord;
    private Rectangle rectangleSurfaceCreationIndicator;

    private List<AttachmentPointUI> irregularSurfaceSummits = new ArrayList<>();
    private double samePositionTolerance = 10;

    public TextField minInspectionLengthTextField;
    public Button inspectButton;
    public TextArea inspectionArea;
    private Double minInspectionLength;

    private Controller domainController = Controller.getInstance();

    Alert patternPreconditionAlert = new Alert(Alert.AlertType.INFORMATION);

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("Initialization...");

        // Invisible Pane object that contains all other shapes
        // Needed to be invisible so zooming out of it would not expose its edge
        // Make it look like its infinite in size
        drawingSection.setPrefHeight(1);
        drawingSection.setPrefWidth(1);
        materialColorChoiceBox.setItems(possibleColor);
        sealColorChoiceBox.setItems(possibleColor);
        tilePatternInputBox.setItems(PatternHelperUI.getPossibleTilePatterns());

        materialNameColumn.setCellValueFactory(new PropertyValueFactory<MaterialUI, String>("name"));
        materialNumberOfBoxInputColumn.setCellValueFactory(new PropertyValueFactory<MaterialUI,String>("numberOfBoxes"));
        materialTilePerBoxColumn.setCellValueFactory(new PropertyValueFactory<MaterialUI,String>("tilePerBox"));
        materialColorColumn.setCellValueFactory(new PropertyValueFactory<MaterialUI,String>("color"));
        materialPricePerBoxColumn.setCellValueFactory(new PropertyValueFactory<MaterialUI,String>("pricePerBoxe"));
        materialTotalPriceColumn.setCellValueFactory(new PropertyValueFactory<MaterialUI,String>("totalPrice"));
        nbTileColumn.setCellValueFactory(new PropertyValueFactory<MaterialUI,String>("numberOfTiles"));

        tileWidthInputbox.setDisable(true);
        tileHeightInputbox.setDisable(true);

        this.snapGridUI = new SnapGridUI(this.drawingSection);
        this.selectionManager = new SelectionManager(this::handleSelection);

        this.undoButton.setDisable(!this.domainController.undoAvailable());
        this.redoButton.setDisable(!this.domainController.redoAvailable());

        this.pane.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
            if (this.stateCurrentlyCreatingRectangularSurface) {
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
//                minInspectionLength = minInspectionLengthInput.toString().equals("") ? null : Double.valueOf(minInspectionLengthInput.toString()).doubleValue();

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
            }else {
                inspectButton.setDisable(true);
            }
        });

        patternPreconditionAlert.setTitle("WARNING");

        defaultMaterial();
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
        if(e.getCode() == KeyCode.CONTROL) {
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
        List<SurfaceUI> temp = new ArrayList<>();
        this.getAccountingForSelectedSurface(temp);
        hideRectangleInfo();
    }

    private void handleRectangularSurfaceCreation(Point clickCoord) {
        if (!stateTopLeftCornerCreated) {
            firstClickCoord = new Point(clickCoord.x, clickCoord.y);
            stateTopLeftCornerCreated = true;
            return;
        }

        stateCurrentlyCreatingRectangularSurface = false;
        stateTopLeftCornerCreated = false;

        if (!clickCoord.isSame(firstClickCoord)) {
            createRectangularSurfaceHere(new Point(firstClickCoord.x, firstClickCoord.y), new Point(clickCoord.x, clickCoord.y) );
        }

        drawingSection.getChildren().remove(rectangleSurfaceCreationIndicator);
        pane.setCursor(Cursor.DEFAULT);
        this.renderFromProject();
        selectionManager.unselectAll();
        hideRectangleInfo();
        firstClickCoord = null;
    }

    private void handleIrregularSurfaceCreation(Point clickCoord) {

        Point pointToAdd = clickCoord;

        boolean isFirstPoint = irregularSurfaceSummits.size() < 1;
        Cursor cursor = isFirstPoint ? Cursor.HAND : null;

        AttachmentPointUI point = new AttachmentPointUI(pointToAdd, null, null, cursor);
        irregularSurfaceSummits.add(point);
        this.drawingSection.getChildren().add(point.getNode());

        if (isFirstPoint) {
            return;
        }

        AttachmentPointUI firstPoint = irregularSurfaceSummits.get(0);
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
        hideRectangleInfo();
    }

    public Void handleSelection(Void nothing) {
        displayRectangleInfo();

        if (selectionManager.getSelectedSurfaces().stream().allMatch(s -> s.toDto().isHole  == HoleStatus.FILLED)) {
            stateCurrentlyFilling = false;
            fillTilesButton.setText("Unfill tiles");
        } else {
            stateCurrentlyFilling = true;
            fillTilesButton.setText("Fill tiles");
        }

        if (selectionManager.getSelectedSurfaces().stream().allMatch(s -> s.toDto().isFusionned)) {
            stateCurrentlyFusionning = false;
            fusionButton.setText("UnFusion surfaces");
        } else {
            stateCurrentlyFusionning = true;
            fusionButton.setText("Fusion surfaces");
        }
        return null;
    }

    public void editSurface() {
        List<SurfaceUI> selectedSurfaces = selectionManager.getSelectedSurfaces();

        if(selectedSurfaces.size() != 0){
            SurfaceUI chosenSurface = selectedSurfaces.get(0);
            NumberFormat format = NumberFormat.getInstance(Locale.FRANCE);
            try{

//                new Tile Height
//                CharSequence tileHeightInput = this.tileHeightInputbox.getCharacters();
//                Double newTileHeight = tileHeightInput.toString().equals("") ? null : format.parse(tileHeightInput.toString()).doubleValue();
////                newTileHeight = tileHeightInput.toString().equals("") ? null : Double.valueOf(tileHeightInput.toString()).doubleValue();
//
//                //ce block nest pu utile a cause de la propriété tiletypeWidth
//                //new Tile Width
//                CharSequence tileWidthInput = this.tileWidthInputbox.getCharacters();
//                Double newTileWidth = tileWidthInput.toString().equals("") ? null : format.parse(tileWidthInput.toString()).doubleValue();
////                newTileWidth = tileWidthInput.toString().equals("") ? null : Double.valueOf(tileWidthInput.toString()).doubleValue();

                //new master Tile position
                CharSequence masterTileXInput = this.masterTileX.getCharacters();
                Double newMasterTileX = masterTileXInput.toString().equals("") ? null : format.parse(masterTileXInput.toString()).doubleValue();
//                newMasterTileX = masterTileXInput.toString().equals("") ? null : Double.valueOf(masterTileXInput.toString()).doubleValue();

                CharSequence masterTileYInput = this.masterTileY.getCharacters();
                Double newMasterTileY = masterTileYInput.toString().equals("") ? null : format.parse(masterTileYInput.toString()).doubleValue();
//                newMasterTileY = masterTileYInput.toString().equals("") ? null : Double.valueOf(masterTileYInput.toString()).doubleValue();

                //new Seal Width
                CharSequence sealWidthInput = this.sealWidthInputBox.getCharacters();
                Double newSealWidth = sealWidthInput.toString().equals("") ? null : format.parse(sealWidthInput.toString()).doubleValue();
//                newSealWidth = sealWidthInput.toString().equals("") ? null : Double.valueOf(sealWidthInput.toString()).doubleValue();

                CharSequence sealColorInput = this.sealColorChoiceBox.getValue();

                CharSequence patternInput = this.tilePatternInputBox.getValue();

                //new surface height
                CharSequence surfaceHeightInput = this.surfaceHeightInputBox.getCharacters();
                double newsurfaceHeight = format.parse(surfaceHeightInput.toString()).doubleValue();
//                newsurfaceHeight = Double.valueOf(surfaceHeightInput.toString()).doubleValue();

                //new surface width
                CharSequence surfaceWidthInput = this.surfaceWidthInputBox.getCharacters();
                double newSurfaceWidth = format.parse(surfaceWidthInput.toString()).doubleValue();
//                newSurfaceWidth = Double.valueOf(surfaceWidthInput.toString()).doubleValue();

                TileDto masterTile = null;
                if (newMasterTileX != null && newMasterTileY != null) {
                    MaterialDto chosenMaterial = domainController
                            .getProject()
                            .materials
                            .stream()
                            .filter(m -> m.name.equals(tileMaterialChoiceBox.getValue()))
                            .findFirst().get();

                    masterTile = new TileDto();
                    masterTile.material = chosenMaterial;

                    RectangleInfo masterTileRect = new RectangleInfo(new Point(newMasterTileX, newMasterTileY), chosenMaterial.tileTypeWidth, chosenMaterial.tileTypeHeight);
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

                chosenSurface.setSize(newSurfaceWidth, newsurfaceHeight);
                if (masterTile == null || newSealWidth == null || sealColorInput == null || patternInput == null) {
                    this.domainController.updateSurface(chosenSurface.toDto());
                } else {
                    SealsInfoDto sealsInfoDto = new SealsInfoDto();
                    sealsInfoDto.sealWidth = newSealWidth;
                    sealsInfoDto.color = ColorHelper.stringToUtils(sealColorChoiceBox.getValue());
                    chosenSurface.setSealsInfo(sealsInfoDto);

                    PatternType pattern = PatternHelperUI.stringToPattern(patternInput.toString());

                    PatternType fallBackPattern = chosenSurface.getPattern() == null ? PatternType.DEFAULT : chosenSurface.getPattern();
                    pattern = checkPattern(pattern, fallBackPattern, masterTile);
                    chosenSurface.setPattern(pattern);
                    this.domainController.updateAndRefill(chosenSurface.toDto(), masterTile, pattern, sealsInfoDto);
                }

                this.renderFromProject();
                hideRectangleInfo();
            }
            catch (ParseException e) {
                displayRectangleInfo();
            }
            catch (NumberFormatException e) {
                displayRectangleInfo();
            }
        }

    }

    private PatternType checkPattern(PatternType newPattern, PatternType previous, TileDto masterTIile) {
        if (newPattern == PatternType.MIX || newPattern == PatternType.GROUP_MIX) {
            RectangleInfo tileRect = RectangleHelper.summitsToRectangleInfo(masterTIile.summits);

            double minSide = Math.min(tileRect.height, tileRect.width);
            double maxSide = Math.min(tileRect.height, tileRect.width);
            boolean isAllowed = (maxSide - 2 * minSide) < Point.DOUBLE_TOLERANCE;

            if (!isAllowed) {
                patternPreconditionAlert.setHeaderText(String.format("You can select pattern '%s' only if the tile width is half of its length...",
                        PatternHelperUI.patternToDisplayString(newPattern)));
                patternPreconditionAlert.setContentText(String.format("Falling back on pattern '%s'", PatternHelperUI.patternToDisplayString(previous)));
                patternPreconditionAlert.showAndWait();
                return previous;
            }
        }
        return newPattern;
    }

    private void displayRectangleInfo() {
        List<SurfaceUI> selectedSurfaces = selectionManager.getSelectedSurfaces();
        SurfaceUI firstOne = selectedSurfaces.get(0);

        NumberFormat formatter = new DecimalFormat("#0.000");

        double height = ShapeHelper.getHeight(new AbstractShape(firstOne.toDto().summits, false));
        double width = ShapeHelper.getWidth(new AbstractShape(firstOne.toDto().summits, false));
        Point topLeft = ShapeHelper.getTopLeftCorner(new AbstractShape(firstOne.toDto().summits, false));

        surfaceHeightInputBox.setText(formatter.format(height));
        surfaceWidthInputBox.setText(formatter.format(width));

        if(firstOne.getSealsInfo() != null) {
            sealWidthInputBox.setText(formatter.format(firstOne.getSealsInfo().sealWidth));
            sealColorChoiceBox.setValue(ColorHelper.utilsColorToString(firstOne.getSealsInfo().color));
        }

        if(firstOne.getPattern() != null) {
            PatternType pattern = firstOne.getPattern();
            tilePatternInputBox.setValue(PatternHelperUI.patternToDisplayString(pattern));
        } else if (firstOne.toDto().isHole == HoleStatus.FILLED) {
            tilePatternInputBox.setValue(PatternHelperUI.getPlaceHolder());
        } else {
            tilePatternInputBox.setValue("");
        }

        surfacePositionXInputBox.setText(formatter.format(topLeft.x));
        surfacePositionYInputBox.setText(formatter.format(topLeft.y));

        if(firstOne.toDto().isHole == HoleStatus.FILLED) {
            String tileMaterial = firstOne.toDto().tiles.get(0).material.name;
            utils.Color tilecolor = firstOne.toDto().tiles.get(0).material.color;
            String materialColor;
            tileMaterialChoiceBox.setValue(tileMaterial);

            materialColor = ColorHelper.utilsColorToString(tilecolor);
            RectangleInfo tileRect = RectangleHelper.summitsToRectangleInfo(firstOne.getMasterTile().summits);

            tileHeightInputbox.setText(formatter.format(tileRect.height));
            tileWidthInputbox.setText(formatter.format(tileRect.width));
            masterTileX.setText(formatter.format(tileRect.topLeftCorner.x));
            masterTileY.setText(formatter.format(tileRect.topLeftCorner.y));
            materialColorDisplay.setText(materialColor);
        } else if (firstOne.toDto().isHole == HoleStatus.HOLE) {
            materialColorDisplay.setText("hole");
        } else {
            materialColorDisplay.setText("");
        }
        this.getAccountingForSelectedSurface(selectedSurfaces);

    }

    private void hideRectangleInfo(){
        tileHeightInputbox.clear();
        tileWidthInputbox.clear();
        surfaceHeightInputBox.clear();
        surfaceWidthInputBox.clear();
        surfacePositionXInputBox.clear();
        surfacePositionYInputBox.clear();
        materialColorDisplay.clear();
        sealWidthInputBox.clear();

        masterTileX.clear();
        masterTileY.clear();

        tilePatternInputBox.setValue("");
        sealColorChoiceBox.setValue("");
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
        if(!stateCurrentlyCreatingRectangularSurface) {
            stateCurrentlyCreatingRectangularSurface = true;
            pane.setCursor(Cursor.CROSSHAIR);
        }
    }

    public void onCreateIrregularSurfaceSelected() {
        if(!stateCurrentlyCreatingIrregularSurface) {
            stateCurrentlyCreatingIrregularSurface = true;
            pane.setCursor(Cursor.CROSSHAIR);
        }
    }

    public void snapGridToggle() {
        // TODO: ajouter un input box pour la taille des espaces snapgrid
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

    public void toggleFill() {
        if (stateCurrentlyFilling) {
            fillSelectedSurfaceWithTiles();
        } else {
            unfillTiles();
        }
    }

    public void fillSelectedSurfaceWithTiles() {
        List<SurfaceUI> selectedSurfaces = this.selectionManager.getSelectedSurfaces();


        for (SurfaceUI surface: selectedSurfaces) {
            surface.forceFill();
        }

        this.renderFromProject();
    }

    public void unfillTiles() {
        List<SurfaceUI> selectedSurfaces = this.selectionManager.getSelectedSurfaces();

        for (SurfaceUI surface: selectedSurfaces) {
            surface.setHole(HoleStatus.NONE);
            domainController.updateSurface(surface.toDto());
            surface.hideTiles();
        }
        hideRectangleInfo();
        this.renderFromProject();
    }

    public void setHole() {
        List<SurfaceUI> selectedSurfaces = this.selectionManager.getSelectedSurfaces();

        for (SurfaceUI surface: selectedSurfaces) {
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
        // TODO : Quand on aggrandit une surface le accounting update pas
        this.clearDrawings();

        this.undoButton.setDisable(!this.domainController.undoAvailable());
        this.redoButton.setDisable(!this.domainController.redoAvailable());

        ProjectDto project = this.domainController.getProject();
        if (project.surfaces != null) {
            for (SurfaceDto surface: project.surfaces) {
                this.displaySurface(surface);
            }
        }

        if (project.materials != null) {
            this.tileMaterialChoiceBox.getItems().clear();
            for (MaterialDto mDto: project.materials) {

                tileMaterialChoiceBox.getItems().add(mDto.name);
            }
            /**
            domainController.getAccounting();
            List<Accounting> account = domainController.Maccount;
            for (Accounting  accounting : account) {

                NumberFormat formatter = new DecimalFormat("#0.000");

                MaterialUI materialUI = new MaterialUI();
                materialUI.name = accounting.getMaterial().getMaterialName();
                materialUI.pricePerBoxe = formatter.format(accounting.getMaterial().getCostPerBox());
                materialUI.color = ColorHelper.utilsColorToString(accounting.getMaterial().getColor());
                materialUI.tilePerBox = formatter.format(accounting.getMaterial().getNbTilePerBox());
                materialUI.numberOfTiles = formatter.format(accounting.getUsedTiles());
                materialUI.numberOfBoxes = formatter.format(accounting.getNbBoxes());
                materialUI.totalPrice = formatter.format(accounting.getTotalCost());


                materialTableView.getItems().add(materialUI);
            }*/
            List<SurfaceUI> temp = new ArrayList<>();
            getAccountingForSelectedSurface(temp);
        }
    }

    private void clearDrawings() {
        this.allSurfaces.forEach(SurfaceUI::hide);
        drawingSection.getChildren().removeIf(allSurfaces.stream().map(s -> s.getNode()).collect(Collectors.toList())::contains);
        this.selectionManager.unselectAll();
        this.allSurfaces.clear();
        this.snapGridUI.renderForViewBox(this.getViewBoxSummits());
        this.materialTableView.getItems().clear();
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

        try{
            NumberFormat format = NumberFormat.getInstance(Locale.FRANCE);

            MaterialDto dto = new MaterialDto();
            dto.materialType = MaterialType.tileMaterial;
            dto.name = materialNameInputBox.getText();
            dto.color = ColorHelper.stringToUtils(materialColorChoiceBox.getValue());

            CharSequence boxCost = this.boxPriceInputBox.getCharacters();
            dto.costPerBox = boxCost.toString().equals("") ? 0 : format.parse(boxCost.toString()).doubleValue();
            dto.costPerBox = boxCost.toString().equals("") ? 0 : Double.parseDouble(boxCost.toString());

            CharSequence tilePerBox = this.tilePerBoxInputBox.getCharacters();
            dto.nbTilePerBox = tilePerBox.toString().equals("") ? 0 : format.parse(tilePerBox.toString()).intValue();
//            dto.nbTilePerBox = tilePerBox.toString().equals("") ? 0 : Double.valueOf(tilePerBox.toString()).intValue();

            CharSequence tileHeight = this.tileHeightMaterialInputBox.getCharacters();
            dto.tileTypeHeight = tileHeight.toString().equals("") ? 0 : format.parse(tileHeight.toString()).doubleValue();
//            dto.tileTypeHeight = tileHeight.toString().equals("") ? 0 : Double.valueOf(tileHeight.toString());

            CharSequence tileWidth = this.tileWidthMaterialInputBox.getCharacters();
            dto.tileTypeWidth = tileWidth.toString().equals("") ? 0 : format.parse(tileWidth.toString()).doubleValue();
//            dto.tileTypeWidth = tileWidth.toString().equals("") ? 0 : Double.valueOf(tileWidth.toString());

            domainController.createMaterial(dto);

            materialNameInputBox.clear();
            boxPriceInputBox.clear();
            tilePerBoxInputBox.clear();
            tileHeightMaterialInputBox.clear();
            tileWidthMaterialInputBox.clear();


            renderFromProject();
        }
        catch (ParseException e){
            displayRectangleInfo();
        }

    }

    public void undo() {
        this.domainController.undo();
        renderFromProject();
    }

    public void redo() {
        this.domainController.redo();
        renderFromProject();
    }

    private void defaultMaterial() {
        MaterialDto dto = new MaterialDto();
        dto.color = Color.GREEN;
        dto.name = "Melon d'eau";
        dto.materialType = MaterialType.tileMaterial;
        dto.nbTilePerBox = 45;
        dto.costPerBox = 50;
        dto.tileTypeHeight = 0.3;
        dto.tileTypeWidth = 0.6;
        domainController.createMaterial(dto);
    }

    public void centerSurfacesVertically() {
        if(this.selectionManager.getSelectedSurfaces().size() <= 1){
            return;
        }

        List<SurfaceUI> selectedSurfaces = this.selectionManager.getSelectedSurfaces();
        SurfaceUI mainSurface = selectedSurfaces.get(0);

        AbstractShape firstSurface = new AbstractShape(mainSurface.toDto().summits);
        Point firstTopLeft = ShapeHelper.getTopLeftCorner(firstSurface);
        double firstX = firstTopLeft.x;
        double centerX = ShapeHelper.getWidth(firstSurface)/2;

        for (SurfaceUI s : selectedSurfaces) {
            if (s == mainSurface) {
                continue;
            }
            AbstractShape surface = new AbstractShape(s.toDto().summits);
            Point surfaceTopLeft = ShapeHelper.getTopLeftCorner(surface);
            double surfaceHalfWidth = ShapeHelper.getWidth(surface)/2;
            double surfaceY = surfaceTopLeft.y;
            s.setPosition(new Point((firstX + centerX) - surfaceHalfWidth, surfaceY));
            if (s.toDto().isHole == HoleStatus.FILLED) {
                domainController.updateAndRefill(s.toDto(), s.getMasterTile(), s.getPattern(), s.getSealsInfo());
            } else {
                domainController.updateSurface(s.toDto());
            }
        }
        this.renderFromProject();
    }

    public void centerSurfacesHorizontally(){
        if(this.selectionManager.getSelectedSurfaces().size() <= 1){
            return;
        }

        List<SurfaceUI> selectedSurfaces = this.selectionManager.getSelectedSurfaces();
        SurfaceUI mainSurface = selectedSurfaces.get(0);

        AbstractShape firstSurface = new AbstractShape(mainSurface.toDto().summits);
        Point firstTopLeft = ShapeHelper.getTopLeftCorner(firstSurface);
        double firstY = firstTopLeft.y;
        double centerY = ShapeHelper.getHeight(firstSurface)/2;

        for(SurfaceUI s: selectedSurfaces){
            if(s == mainSurface){
                continue;
            }

            AbstractShape surface = new AbstractShape(s.toDto().summits);
            Point surfaceTopLeft = ShapeHelper.getTopLeftCorner(surface);
            double surfaceX = surfaceTopLeft.x;
            double halfHeight = ShapeHelper.getHeight(surface)/2;
            s.setPosition(new Point(surfaceX, (firstY + centerY) - halfHeight));
            if (s.toDto().isHole == HoleStatus.FILLED) {
                domainController.updateAndRefill(s.toDto(), s.getMasterTile(), s.getPattern(), s.getSealsInfo());
            } else {
                domainController.updateSurface(s.toDto());
            }
        }
        this.renderFromProject();
    }

    public void alignLeftSurfaces() {
        if(this.selectionManager.getSelectedSurfaces().size() <= 1){
            return;
        }
        List<SurfaceUI> selectedSurfaces = this.selectionManager.getSelectedSurfaces();
        SurfaceUI mainSurface = selectedSurfaces.get(0);

        AbstractShape firstSurface = new AbstractShape(mainSurface.toDto().summits);
        Point topLeft = ShapeHelper.getTopLeftCorner(firstSurface);
        double firstX = topLeft.x;
        double firstY =topLeft.y;

        for(SurfaceUI s: selectedSurfaces){
            if(s == mainSurface){
                continue;
            }
            AbstractShape surface = new AbstractShape(s.toDto().summits);
            double surfaceWidth = ShapeHelper.getWidth(surface);
            s.setPosition(new Point(firstX - surfaceWidth - 0.25, firstY));

            if (s.toDto().isHole == HoleStatus.FILLED) {
                domainController.updateAndRefill(s.toDto(), s.getMasterTile(), s.getPattern(), s.getSealsInfo());
            } else {
                domainController.updateSurface(s.toDto());
            }
        }
        this.renderFromProject();
    }

    public void alignRightSurfaces(){
        if(this.selectionManager.getSelectedSurfaces().size() <= 1){
            return;
        }
        List<SurfaceUI> selectedSurfaces = this.selectionManager.getSelectedSurfaces();
        SurfaceUI mainSurface = selectedSurfaces.get(0);

        AbstractShape firstSurface = new AbstractShape(mainSurface.toDto().summits);
        Point topLeft = ShapeHelper.getTopLeftCorner(firstSurface);
        double firstX = topLeft.x;
        double firstY = topLeft.y;
        double firstWidth = ShapeHelper.getWidth(firstSurface);

        for(SurfaceUI s: selectedSurfaces){
            if(s == mainSurface){
                continue;
            }
            AbstractShape surface = new AbstractShape(s.toDto().summits);
            s.setPosition(new Point(firstX + firstWidth + 0.25, firstY));

            if (s.toDto().isHole == HoleStatus.FILLED) {
                domainController.updateAndRefill(s.toDto(), s.getMasterTile(), s.getPattern(), s.getSealsInfo());
            } else {
                domainController.updateSurface(s.toDto());
            }
        }
        this.renderFromProject();
    }

    public void alignTopSurfaces(){
        if(this.selectionManager.getSelectedSurfaces().size() <= 1){
            return;
        }
        List<SurfaceUI> selectedSurfaces = this.selectionManager.getSelectedSurfaces();
        SurfaceUI mainSurface = selectedSurfaces.get(0);

        AbstractShape firstSurface = new AbstractShape(mainSurface.toDto().summits);
        Point topLeft = ShapeHelper.getTopLeftCorner(firstSurface);
        double firstX = topLeft.x;
        double firstY = topLeft.y;

        for(SurfaceUI s: selectedSurfaces){
            if(s == mainSurface){
                continue;
            }
            AbstractShape surface = new AbstractShape(s.toDto().summits);
            double surfaceHeight = ShapeHelper.getHeight(surface);
            s.setPosition(new Point(firstX, firstY - surfaceHeight - 0.25));

            if (s.toDto().isHole == HoleStatus.FILLED) {
                domainController.updateAndRefill(s.toDto(), s.getMasterTile(), s.getPattern(), s.getSealsInfo());
            } else {
                domainController.updateSurface(s.toDto());
            }
        }
        this.renderFromProject();
    }

    public void alignBottomSurfaces(){
        if(this.selectionManager.getSelectedSurfaces().size() <= 0){
            return;
        }
        List<SurfaceUI> selectedSurfaces = this.selectionManager.getSelectedSurfaces();
        SurfaceUI mainSurface = selectedSurfaces.get(0);

        AbstractShape firstSurface = new AbstractShape(mainSurface.toDto().summits);
        Point topLeft = ShapeHelper.getTopLeftCorner(firstSurface);
        double firstX = topLeft.x;
        double firstY = topLeft.y;
        double firstHeight = ShapeHelper.getHeight(firstSurface);

        for(SurfaceUI s: selectedSurfaces){
            if(s == mainSurface){
                continue;
            }
            AbstractShape surface = new AbstractShape(s.toDto().summits);
            s.setPosition(new Point(firstX, firstY + firstHeight + 0.25));

            if (s.toDto().isHole == HoleStatus.FILLED) {
                domainController.updateAndRefill(s.toDto(), s.getMasterTile(), s.getPattern(), s.getSealsInfo());
            } else {
                domainController.updateSurface(s.toDto());
            }
        }
        this.renderFromProject();
    }

    public void stickSurfacesVertically() {
        if(this.selectionManager.getSelectedSurfaces().size() <= 1){
            return;
        }

        List<SurfaceUI> selectedSurfaces = this.selectionManager.getSelectedSurfaces();
        SurfaceUI mainSurface = selectedSurfaces.get(0);

        AbstractShape firstSurface = new AbstractShape(mainSurface.toDto().summits);
        Point firstTopLeft = ShapeHelper.getTopLeftCorner(firstSurface);
        double firstY = firstTopLeft.y;
        double firstHeight = ShapeHelper.getHeight(firstSurface);

        for(SurfaceUI s : selectedSurfaces){
            if(s == mainSurface){
                continue;
            }
            AbstractShape surface = new AbstractShape(s.toDto().summits);
            Point surfaceTopLeft = ShapeHelper.getTopLeftCorner(surface);
            double surfaceX = surfaceTopLeft.x;
            double surfaceY = surfaceTopLeft.y;
            double surfaceHeight = ShapeHelper.getHeight(surface);

            if(surfaceY > firstY) {
                s.setPosition(new Point(surfaceX, firstY + firstHeight));
            }
            else{
                s.setPosition(new Point(surfaceX, firstY - surfaceHeight));
            }

            if (s.toDto().isHole == HoleStatus.FILLED) {
                domainController.updateAndRefill(s.toDto(), s.getMasterTile(), s.getPattern(), s.getSealsInfo());
            } else {
                domainController.updateSurface(s.toDto());
            }
        }
        this.renderFromProject();
    }

    public void stickSurfacesHorizontally(){
        if(this.selectionManager.getSelectedSurfaces().size() <= 1){
            return;
        }
        List<SurfaceUI> selectedSurfaces = this.selectionManager.getSelectedSurfaces();
        SurfaceUI mainSurface = selectedSurfaces.get(0);

        AbstractShape firstSurface = new AbstractShape(mainSurface.toDto().summits);
        Point firstTopLeft = ShapeHelper.getTopLeftCorner(firstSurface);
        double firstX = firstTopLeft.x;
        double firstWidth = ShapeHelper.getWidth(firstSurface);

        for(SurfaceUI s: selectedSurfaces){
            if(s == mainSurface){
                continue;
            }
            AbstractShape surface = new AbstractShape(s.toDto().summits);
            Point surfaceTopLeft = ShapeHelper.getTopLeftCorner(surface);
            double surfaceX = surfaceTopLeft.x;
            double surfaceY = surfaceTopLeft.y;
            double surfaceWidth = ShapeHelper.getWidth(surface);
            if(surfaceX > firstX){
                s.setPosition(new Point(firstX + firstWidth, surfaceY));
            }
            else{
                s.setPosition(new Point(firstX - surfaceWidth, surfaceY));
            }

            if (s.toDto().isHole == HoleStatus.FILLED) {
                domainController.updateAndRefill(s.toDto(), s.getMasterTile(), s.getPattern(), s.getSealsInfo());
            } else {
                domainController.updateSurface(s.toDto());
            }
        }
        this.renderFromProject();
    }

    public void inspect() {

        String inspectionResult = domainController.inspectProject(minInspectionLength, minInspectionLength);
        inspectionArea.setText(String.format("Inspection result for min lenght = %.2f m : \n\n%s", minInspectionLength, inspectionResult));
        this.renderFromProject();
    }

    public void SaveProject(){
        // TODO: Linker le path au fichier a save
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sauvegarder Plan");
        fileChooser.setInitialDirectory(new File("./"));
        fileChooser.setInitialFileName("TaMereEnShort.bin");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Java Serialized object", "*.bin"));
        File file = fileChooser.showSaveDialog(null);
        if (file != null){
            domainController.saveProject(file.getPath().toString());
        }

    }

    public void LoadProject(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load Plan");
        fileChooser.setInitialDirectory(new File("./"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Java Serialized object", "*.bin"));
        File file = fileChooser.showOpenDialog(null);
        if (file != null){
            domainController.loadProject(file.getPath().toString());
        }

        this.renderFromProject();
    }

    public void newProject(){
        domainController.newProject();
        renderFromProject();
    }

    private boolean createMaterialListener(){
        boolean parseSucess = true;
        if(materialNameInputBox.getText() == null){
            parseSucess = false;
        }
        return parseSucess;
    }

    private void getAccountingForSelectedSurface(List<SurfaceUI> pSelectedSurfaces){
        this.materialTableView.getItems().clear();

            List<SurfaceDto> listDTO = new ArrayList<>();
            for (SurfaceUI i : pSelectedSurfaces) {
                if (i.toDto().isHole == HoleStatus.FILLED) {
                    listDTO.add(i.toDto());
                }
            }
            if(listDTO.size() == 0){
                domainController.getAccounting();
            }else{
                domainController.getSurfaceAccount(listDTO);
            }
            List<Accounting> account = domainController.Maccount;
            for (Accounting accounting : account) {

                NumberFormat formatter = new DecimalFormat("#0.000");

                MaterialUI materialUI = new MaterialUI();
                materialUI.name = accounting.getMaterial().getMaterialName();
                materialUI.pricePerBoxe = formatter.format(accounting.getMaterial().getCostPerBox());
                materialUI.color = ColorHelper.utilsColorToString(accounting.getMaterial().getColor());
                materialUI.tilePerBox = formatter.format(accounting.getMaterial().getNbTilePerBox());
                materialUI.numberOfTiles = formatter.format(accounting.getUsedTiles());
                materialUI.numberOfBoxes = formatter.format(accounting.getNbBoxes());
                materialUI.totalPrice = formatter.format(accounting.getTotalCost());


                materialTableView.getItems().add(materialUI);
            }
    }
}
