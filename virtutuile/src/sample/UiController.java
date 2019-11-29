package sample;

import Domain.HoleStatus;
import Domain.MaterialType;
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
import utils.*;

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
    private ObservableList<String> possibleColor = FXCollections.observableArrayList("BLACK","WHITE","YELLOW","GREEN","BLUE","RED","VIOLET");
    private ObservableList<String> tilePattern = FXCollections.observableArrayList("Default","Shift","Diagonal","Spinning","X");

    public TextField materialNameInputBox;
    public TextField tilePerBoxInputBox;
    public TextField boxPriceInputBox;

    public ChoiceBox<String> materialColorChoiceBox;

    public TableView<MaterialUI> materialTableView;
    public TableColumn<MaterialUI,String> materialNameColumn;
    public TableColumn<MaterialUI,String> materialNumberOfBoxInputColumn;
    public TableColumn<MaterialUI,String> materialTilePerBoxColumn;
    public TableColumn<MaterialUI,String> materialColorColumn;
    public TableColumn<MaterialUI,String> materialPricePerBoxColumn;
    public TableColumn<MaterialUI,String> materialTotalPriceColumn;

    // surface properties inputs
    public TextField tileHeightInputbox;
    public TextField tileWidthInputbox;
    public TextField sealWidthInputBox;
    public TextField surfaceHeightInputBox;
    public TextField surfaceWidthInputBox;
    public TextField surfacePositionXInputBox;
    public TextField surfacePositionYInputBox;

    public Button fillTilesButton;
    public boolean stateCurrentlyFilling = true;

    public TextField materialColorDisplay;

    public Button fusionButton;
    public boolean stateCurrentlyFusionning = true;

    public Label tileInfo;

    public ChoiceBox<String> sealColorChoiceBox;
    public ChoiceBox<String> sealPatternInputBox;
    public ChoiceBox<String> tileMaterialChoiceBox;

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
    private Rectangle rectangleSurfaceCreationIndicator;

    public TextField minInspectionLengthTextField;
    public Button inspectButton;
    public TextArea inspectionArea;
    private Double minInspectionLength;

    private Controller domainController = Controller.getInstance();

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
        sealPatternInputBox.setItems(tilePattern);

        materialNameColumn.setCellValueFactory(new PropertyValueFactory<MaterialUI, String>("name"));
        materialNumberOfBoxInputColumn.setCellValueFactory(new PropertyValueFactory<MaterialUI,String>("numberOfBoxes"));
        materialTilePerBoxColumn.setCellValueFactory(new PropertyValueFactory<MaterialUI,String>("tilePerBox"));
        materialColorColumn.setCellValueFactory(new PropertyValueFactory<MaterialUI,String>("color"));
        materialPricePerBoxColumn.setCellValueFactory(new PropertyValueFactory<MaterialUI,String>("pricePerBoxe"));
        materialTotalPriceColumn.setCellValueFactory(new PropertyValueFactory<MaterialUI,String>("totalPrice"));

        this.snapGridUI = new SnapGridUI(this.drawingSection);
        this.selectionManager = new SelectionManager(this::handleSelection);

        this.undoButton.setDisable(!this.domainController.undoAvailable());
        this.redoButton.setDisable(!this.domainController.redoAvailable());

        this.pane.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
            if (this.stateCurrentlyCreatingSurface) {
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

            Point mouseCoord = new Point(e.getX(), e.getY());
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
        if (stateCurrentlyCreatingSurface && !stateTopLeftCornerCreated) {

            firstClickCoord = new Point(clickCoord.x, clickCoord.y);
            stateTopLeftCornerCreated = true;
        }
        else if (stateCurrentlyCreatingSurface && stateTopLeftCornerCreated)
        {
            Point secondClickCoord = clickCoord;
            stateCurrentlyCreatingSurface = false;
            stateTopLeftCornerCreated = false;

            if (!secondClickCoord.isSame(firstClickCoord)) {
                createSurfaceHere(new Point(firstClickCoord.x, firstClickCoord.y), new Point(secondClickCoord.x, secondClickCoord.y) );
            }

            drawingSection.getChildren().remove(rectangleSurfaceCreationIndicator);
            pane.setCursor(Cursor.DEFAULT);
            this.renderFromProject();
            selectionManager.unselectAll();
            hideRectangleInfo();
            firstClickCoord = null;
        }

        stateCurrentlyFilling = true;
        fillTilesButton.setText("Fill tiles");

        stateCurrentlyFusionning = true;
        fusionButton.setText("Fusion surfaces");

        selectionManager.unselectAll();
        hideRectangleInfo();
    }

    public Void handleSelection(boolean isRectangle) {
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

                //new Tile Height
                CharSequence tileHeightInput = this.tileHeightInputbox.getCharacters();
                Double newTileHeight = tileHeightInput.toString().equals("") ? null : format.parse(tileHeightInput.toString()).doubleValue();

                //new Tile Width
                CharSequence tileWidthInput = this.tileWidthInputbox.getCharacters();
                Double newTileWidth = tileWidthInput.toString().equals("") ? null : format.parse(tileWidthInput.toString()).doubleValue();

                //new Seal Width
                CharSequence sealWidthInput = this.sealWidthInputBox.getCharacters();
                Double newSealWidth = sealWidthInput.toString().equals("") ? null : format.parse(sealWidthInput.toString()).doubleValue();

                CharSequence sealColorInput = this.sealColorChoiceBox.getValue();

                //new surface height
                CharSequence surfaceHeightInput = this.surfaceHeightInputBox.getCharacters();
                double newsurfaceHeight = format.parse(surfaceHeightInput.toString()).doubleValue();

                //new surface width
                CharSequence surfaceWidthInput = this.surfaceWidthInputBox.getCharacters();
                double newSurfaceWidth = format.parse(surfaceWidthInput.toString()).doubleValue();

                TileDto masterTile = null;
                if (newTileWidth != null && newTileHeight != null) {
                    MaterialDto chosenMaterial = domainController
                            .getProject()
                            .materials
                            .stream()
                            .filter(m -> m.name.equals(tileMaterialChoiceBox.getValue()))
                            .findFirst().get();

                    masterTile = new TileDto();
                    masterTile.material = chosenMaterial;

                    AbstractShape shape = new AbstractShape(chosenSurface.toDto().summits, false);
                    Point topLeftCorner = ShapeHelper.getTopLeftCorner(shape);

                    RectangleInfo masterTileRect = new RectangleInfo(topLeftCorner, newTileWidth, newTileHeight);
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
                if (masterTile == null || newSealWidth == null || sealColorInput == null) {
                    this.domainController.updateSurface(chosenSurface.toDto());
                } else {
                    SealsInfoDto sealsInfoDto = new SealsInfoDto();
                    sealsInfoDto.sealWidth = newSealWidth;
                    sealsInfoDto.color = ColorHelper.stringToUtils(sealColorChoiceBox.getValue());
                    chosenSurface.setSealsInfo(sealsInfoDto);
                    this.domainController.updateAndRefill(chosenSurface.toDto(), masterTile, PatternDto.DEFAULT, sealsInfoDto);
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

        surfacePositionXInputBox.setText(formatter.format(topLeft.x));
        surfacePositionYInputBox.setText(formatter.format(topLeft.y));

        if(firstOne.toDto().isHole == HoleStatus.FILLED){
            String tileMaterial = firstOne.toDto().tiles.get(0).material.name;
            utils.Color tilecolor = firstOne.toDto().tiles.get(0).material.color;
            String materialColor;
            tileMaterialChoiceBox.setValue(tileMaterial);
            sealPatternInputBox.setValue("Default");

            materialColor = ColorHelper.utilsColorToString(tilecolor);
            RectangleInfo tileRect = RectangleHelper.summitsToRectangleInfo(firstOne.toDto().tiles.get(0).summits);

            tileHeightInputbox.setText(formatter.format(tileRect.height / 100));
            tileWidthInputbox.setText(formatter.format(tileRect.width / 100));
            materialColorDisplay.setText(materialColor);
        } else {
            materialColorDisplay.setText("c'est un trou");
        }

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

        renderFromProject();
    }

    public void unfillTiles() {
        List<SurfaceUI> selectedSurfaces = this.selectionManager.getSelectedSurfaces();

        for (SurfaceUI surface: selectedSurfaces) {
            surface.setHole(HoleStatus.NONE);
            domainController.updateSurface(surface.toDto());
            surface.hideTiles();
        }
        hideRectangleInfo();
        renderFromProject();
    }

    public void setHole() {
        List<SurfaceUI> selectedSurfaces = this.selectionManager.getSelectedSurfaces();

        for (SurfaceUI surface: selectedSurfaces) {
            surface.setHole(HoleStatus.HOLE);
            domainController.updateSurface(surface.toDto());
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

        if (project.materials != null) {
            this.tileMaterialChoiceBox.getItems().clear();
            for (MaterialDto mDto: project.materials) {

                MaterialUI materialUI = new MaterialUI();
                materialUI.name = mDto.name;
                materialUI.pricePerBoxe = "50";
                materialUI.color = ColorHelper.utilsColorToString(mDto.color);
                materialUI.tilePerBox = "50";
                materialUI.numberOfBoxes = "50";
                materialUI.totalPrice = "50$";

                materialTableView.getItems().add(materialUI);
                tileMaterialChoiceBox.getItems().add(materialUI.name);
            }
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
        if(materialColorChoiceBox.getValue() != null){
            MaterialDto dto = new MaterialDto();
            dto.materialType = MaterialType.tileMaterial;
            dto.name = materialNameInputBox.getText();
            dto.color = ColorHelper.stringToUtils(materialColorChoiceBox.getValue());
            domainController.createMaterial(dto);

            materialNameInputBox.clear();
            boxPriceInputBox.clear();
            tilePerBoxInputBox.clear();

            renderFromProject();
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
        domainController.createMaterial(dto);
    }

    public void alignSurfacesVertically() {
        if(this.selectionManager.getSelectedSurfaces().size() <= 1){
            return;
        }

        List<SurfaceUI> selectedSurfaces = this.selectionManager.getSelectedSurfaces();
        SurfaceUI mainSurface = selectedSurfaces.get(0);

        RectangleInfo firstRect = RectangleHelper.summitsToRectangleInfo(mainSurface.toDto().summits);
        double firstX = firstRect.topLeftCorner.x;
        double centerX = firstRect.width / 2;

        for (SurfaceUI s : selectedSurfaces) {
            if (s == mainSurface) {
                continue;
            }
            RectangleInfo rect = RectangleHelper.summitsToRectangleInfo(s.toDto().summits);
            double halfWidth = rect.width / 2;
            double rectY = rect.topLeftCorner.y;
            s.setPosition(new Point((firstX + centerX) - halfWidth, rectY));
            domainController.updateAndRefill(s.toDto(), s.getMasterTile(), PatternDto.DEFAULT, s.getSealsInfo());
        }
        this.renderFromProject();
    }

    public void alignSurfacesHorizontally(){
        if(this.selectionManager.getSelectedSurfaces().size() <= 1){
            return;
        }

        List<SurfaceUI> selectedSurfaces = this.selectionManager.getSelectedSurfaces();
        SurfaceUI mainSurface = selectedSurfaces.get(0);

        RectangleInfo firstRect = RectangleHelper.summitsToRectangleInfo(mainSurface.toDto().summits);
        double firstY = firstRect.topLeftCorner.y;
        double centerY = firstRect.height/2;

        for(SurfaceUI s: selectedSurfaces){
            if(s == mainSurface){
                continue;
            }
            RectangleInfo rect = RectangleHelper.summitsToRectangleInfo(s.toDto().summits);
            double halfHeight = rect.height/2;
            double rectX = rect.topLeftCorner.x;
            s.setPosition(new Point(rectX, (firstY + centerY) - halfHeight));
            domainController.updateAndRefill(s.toDto(), s.getMasterTile(), PatternDto.DEFAULT, s.getSealsInfo());
        }
        this.renderFromProject();
    }

    public void inspect() {
        String inspectionResult = domainController.inspect();
        inspectionArea.setText(String.format("Inspection result for min lenght = %.2f m : \n\n%s", minInspectionLength, inspectionResult));
    }
}
