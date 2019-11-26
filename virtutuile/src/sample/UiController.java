package sample;

import Domain.HoleStatus;
import Domain.Material;
import Domain.MaterialType;
import Domain.Project;
import application.*;
import gui.*;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventType;
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


      //  materialTableView = new TableView<>();


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
            stateCurrentlyFilling = true;
            fillTilesButton.setText("Fill tiles");
            hideRectangleInfo();
            firstClickCoord = null;
        }

        selectionManager.unselectAll();
        hideRectangleInfo();
    }

    public Void handleSelection(boolean isRectangle) {
        if (isRectangle) {
            afficherRectangleInfo();
        }

        if (selectionManager.getSelectedSurfaces().get(0).toDto().isHole == HoleStatus.FILLED) {
            stateCurrentlyFilling = false;
            fillTilesButton.setText("Unfill tiles");
        } else {
            stateCurrentlyFilling = true;
            fillTilesButton.setText("Fill tiles");
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
                Color newSealColor = ColorHelper.laTiteStringToUtils(sealColorInput.toString());

                SealsInfoDto newSealInfo = new SealsInfoDto();
                newSealInfo.color = newSealColor;
                newSealInfo.sealWidth = newSealWidth;
                chosenSurface.setSealsInfo(newSealInfo);

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
                    RectangleInfo chosenSurfaceRect = RectangleHelper.summitsToRectangleInfo(chosenSurface.toDto().summits);
                    RectangleInfo masterTileRect = new RectangleInfo(chosenSurfaceRect.topLeftCorner, newTileWidth, newTileHeight);
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
                if (masterTile == null || newSealInfo == null) {
                    this.domainController.updateSurface(chosenSurface.toDto());
                } else {
                    // TODO: Mettre les bonnes infos
                    this.domainController.updateAndRefill(chosenSurface.toDto(), masterTile, PatternDto.DEFAULT, newSealInfo);
                }

//                //Si ce n'est pas un trou
//                if(chosenSurface.toDto().isHole == HoleStatus.FILLED){
//                    chosenSurface.fill();
//                }
                this.renderFromProject();
                hideRectangleInfo();
            }
            catch (ParseException e ) {
                afficherRectangleInfo();
            }
        }

    }

    private void afficherRectangleInfo() {
        List<SurfaceUI> selectedSurfaces = selectionManager.getSelectedSurfaces();
        SurfaceUI firstOne = selectedSurfaces.get(0);

        RectangleInfo rect = RectangleHelper.summitsToRectangleInfo(firstOne.toDto().summits);
        NumberFormat formatter = new DecimalFormat("#0.000");
        surfaceHeightInputBox.setText(formatter.format(rect.height));
        surfaceWidthInputBox.setText(formatter.format(rect.width));

        if(firstOne.getSealsInfo() != null) {
            sealWidthInputBox.setText(formatter.format(firstOne.getSealsInfo().sealWidth));
            sealColorChoiceBox.setValue(ColorHelper.utilsColorToLaTiteString(firstOne.getSealsInfo().color));
        }

        surfacePositionXInputBox.setText(formatter.format(rect.topLeftCorner.x));
        surfacePositionYInputBox.setText(formatter.format(rect.topLeftCorner.y));
        if(firstOne.toDto().isHole == HoleStatus.FILLED){
            String tileMaterial = firstOne.toDto().tiles.get(0).material.name;
            utils.Color tilecolor = firstOne.toDto().tiles.get(0).material.color;
            String materialColor;
            tileMaterialChoiceBox.setValue(tileMaterial);
            //TODO mettre le vrai pattern
            sealPatternInputBox.setValue("Default");


            //TODO Sealinfo ->sealWidth/sealColor n'est jamais modifié
//            sealWidthInputBox.setText(formatter.format(firstOne.getSealsInfo().sealWidth));
//            sealColorChoiceBox.setValue(firstOne.getSealsInfo().sealColor);

            if(tilecolor == Color.BLACK){
                materialColor = "BLACK";
            }else if(tilecolor == Color.WHITE){
                materialColor = "WHITE";
            }else if(tilecolor == Color.YELLOW){
                materialColor = "YELLOW";
            }else if(tilecolor == Color.GREEN){
                materialColor = "GREEN";
            }else if(tilecolor == Color.BLUE){
                materialColor = "BLUE";
            }else if(tilecolor == Color.RED){
                materialColor = "RED";
            }else if(tilecolor == Color.VIOLET){
                materialColor = "VIOLET";
            }else{
                throw new RuntimeException("Les couleurs petent mon gars");
            }
            RectangleInfo tileRect = RectangleHelper.summitsToRectangleInfo(firstOne.toDto().tiles.get(0).summits);

            tileHeightInputbox.setText(formatter.format(tileRect.height/100));
            tileWidthInputbox.setText(formatter.format(tileRect.width/100));
            materialColorDisplay.setText(materialColor);
        }else{
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

//    private void createSurfaceHere(Point location1, Point location2) {
//
//        Point topLeft = RectangleHelper.getTopLeft(location1, location2);
//
//        double widthPixels = 0.0;
//        double heightPixels = 0.0;
//
//        double calculatedWidth = location2.x - location1.y;
//        double calulatedHeight = location2.y - location1.y;
//
//        if(topLeft.x == location1.x && topLeft.y == location1.y){
//            widthPixels = location2.x - topLeft.x;
//            heightPixels = location2.y - topLeft.y;
//        }
//
//        if(topLeft.x == location2.x && topLeft.y == location2.y){
//            widthPixels = location1.x - location2.x;
//            heightPixels = location1.x - location2.x;
//        }
//
//        if(calculatedWidth < 0 && calulatedHeight > 0){
//            widthPixels = location1.x - topLeft.x;
//            heightPixels = location2.y - topLeft.y;
//        }
//
//        if(calculatedWidth > 0 && calulatedHeight < 0){
//            widthPixels = location2.x - topLeft.x;
//            heightPixels = location1.y - topLeft.y;
//        }
//
//        Point desiredPoint1 = new Point(topLeft.x, topLeft.y);
//        Point actualPoint1 = this.snapGridUI.isVisible() ? this.snapGridUI.getNearestGridPoint(desiredPoint1) : desiredPoint1;
//
//        double x = zoomManager.pixelsToMeters(actualPoint1.x);
//        double y = zoomManager.pixelsToMeters(actualPoint1.y);
//        double width = zoomManager.pixelsToMeters(widthPixels);
//        double height = zoomManager.pixelsToMeters(heightPixels);
//
//        SurfaceDto surface = new SurfaceDto();
//        surface.id = new Id();
//        surface.isHole = false;
//        surface.isRectangular = true;
//        surface.summits = RectangleHelper.rectangleInfoToSummits(new Point(x, y), width, height);
//
//        domainController.createSurface(surface);
//    }

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
                String mColor;

                if(mDto.color == Color.BLACK){
                    mColor = "BLACK";

                }else if(mDto.color == Color.WHITE){
                    mColor = "WHITE";

                }else if(mDto.color == Color.YELLOW){
                    mColor = "YELLOW";

                }else if(mDto.color == Color.GREEN){
                    mColor = "GREEN";

                }else if(mDto.color == Color.BLUE){
                    mColor = "BLUE";

                }else if(mDto.color == Color.RED){
                    mColor = "RED";

                }else if(mDto.color == Color.VIOLET){
                    mColor = "VIOLET";

                }else{
                    throw new RuntimeException("Les couleurs petent mon gars");
                }
                //TODO accounting stuff
                MaterialUI materialUI = new MaterialUI();
                materialUI.name = mDto.name;
                materialUI.pricePerBoxe = "50";
                materialUI.color = mColor;
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

    public void surfaceFusion() {
        if (this.selectionManager.getSelectedSurfaces().size() <= 1) {
            return;
        }

        List<SurfaceUI> selectedSurfaces = this.selectionManager.getSelectedSurfaces();
        this.domainController.fusionSurfaces(selectedSurfaces.stream().map(s -> s.toDto()).collect(Collectors.toList()));
        this.renderFromProject();
    }

    public void createNewMaterial(){
        if(materialColorChoiceBox.getValue() != null){
            MaterialDto dto = new MaterialDto();
            dto.materialType = MaterialType.tileMaterial;
            dto.name = materialNameInputBox.getText();
            if(materialColorChoiceBox.getValue() == "BLACK"){
                dto.color = Color.BLACK;

            }else if(materialColorChoiceBox.getValue() == "WHITE"){
                dto.color = utils.Color.WHITE;

            }else if(materialColorChoiceBox.getValue() == "YELLOW"){
                dto.color = utils.Color.YELLOW;

            }else if(materialColorChoiceBox.getValue() == "GREEN"){
                dto.color = utils.Color.GREEN;

            }else if(materialColorChoiceBox.getValue() == "BLUE"){
                dto.color = utils.Color.BLUE;

            }else if(materialColorChoiceBox.getValue() == "RED"){
                dto.color = utils.Color.RED;

            }else if(materialColorChoiceBox.getValue() == "VIOLET"){
                dto.color = utils.Color.VIOLET;

            }else{
                throw new RuntimeException("Les couleurs petent mon gars");
            }
            domainController.createMaterial(dto);
            renderFromProject();
            clearCreatMaterial();

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

    private void defaultMaterial(){
        //TODO ajouter les infos dans accounting

        MaterialDto dto = new MaterialDto();
        dto.color = Color.GREEN;
        dto.name = "Melon d'eau";
        dto.materialType = MaterialType.tileMaterial;

        domainController.createMaterial(dto);
    }

    private void clearCreatMaterial(){
        materialNameInputBox.clear();
        boxPriceInputBox.clear();
        tilePerBoxInputBox.clear();

    }

    private Color choiceBoxToEnum(String pColor){
        Color color;
        if(pColor == "BLACK"){
            color = Color.BLACK;

        }else if(pColor == "WHITE"){
            color = Color.WHITE;

        }else if(pColor == "YELLOW"){
            color = Color.YELLOW;

        }else if(pColor == "GREEN"){
            color = Color.GREEN;

        }else if(pColor == "BLUE"){
            color = Color.BLUE;

        }else if(pColor == "RED"){
            color = Color.RED;

        }else if(pColor == "VIOLET"){
            color = Color.VIOLET;
        }else{
            throw new RuntimeException("Les couleurs petent mon gars");
        }
        return color;

    }
}
