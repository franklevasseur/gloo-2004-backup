package sample;

import Domain.MaterialType;
import Domain.Surface;
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
    public TextField surfacePosotoionXInputBox;
    public TextField surfacePosotoionYInputBox;

    public Label tileInfo;

    public ChoiceBox<String> materialColorEdit;
    public ChoiceBox<String> sealColorChoiceBox;
    public ChoiceBox<String> sealPatternInputBox;
    public ChoiceBox<String> tileMaterialChoiceBox;

    public CheckBox snapGridCheckBox;

    private List<SurfaceUI> allSurfaces = new ArrayList<>();
    private SelectionManager selectionManager;
    private ZoomManager zoomManager = new ZoomManager();
    private SnapGridUI snapGridUI;

    // state variables to make coherent state machine
    private boolean stateCurrentlyCreatingSurface = false;
    private boolean stateEnableZooming = false;

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
        materialColorEdit.setItems(possibleColor);
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

        System.out.println(String.format("click : (%f, %f)", clickCoord.x, clickCoord.y));
        if (stateCurrentlyCreatingSurface) {
            pane.setCursor(Cursor.DEFAULT);
            stateCurrentlyCreatingSurface = false;

            createSurfaceHere(new Point(clickCoord.x, clickCoord.y), 200, 200);

            this.renderFromProject();
        }
        selectionManager.unselectAll();
        hideRectangleInfo();
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
                CharSequence positionXinput = surfacePosotoionXInputBox.getCharacters();
                CharSequence positionYinput = surfacePosotoionYInputBox.getCharacters();
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
                System.out.println("STFU ça pete");
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

        surfacePosotoionXInputBox.setText(formatter.format(rect.topLeftCorner.x));
        surfacePosotoionYInputBox.setText(formatter.format(rect.topLeftCorner.y));


    }

    private void hideRectangleInfo(){

        tileHeightInputbox.clear();
        tileWidthInputbox.clear();
        surfaceHeightInputBox.clear();
        surfaceWidthInputBox.clear();
        surfacePosotoionXInputBox.clear();
        surfacePosotoionYInputBox.clear();
        materialColorEdit.hide();
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

    public void fillSelectedSurfaceWithTiles() {
        List<SurfaceUI> selectedSurfaces = this.selectionManager.getSelectedSurfaces();

        for (SurfaceUI surface: selectedSurfaces) {
            surface.setHole(false);
            domainController.updateSurface(surface.toDto());
            surface.fill();
        }

        renderFromProject();
    }

    private void removeSelectedSurfaces() {
        List<SurfaceUI> selectedSurfaces = selectionManager.getSelectedSurfaces();
        selectedSurfaces.forEach(SurfaceUI::delete);
        allSurfaces.removeIf(selectedSurfaces::contains);
        selectionManager.unselectAll();
    }

    private void createSurfaceHere(Point location, double widthPixels, double heightPixels) {

        double xPixels = location.x - (widthPixels / 2);
        double yPixels = location.y - (heightPixels / 2);

        Point desiredPoint = new Point(xPixels, yPixels);
        Point actualPoint = this.snapGridUI.isVisible() ? this.snapGridUI.getNearestGridPoint(desiredPoint) : desiredPoint;

        double x = zoomManager.pixelsToMeters(actualPoint.x);
        double y = zoomManager.pixelsToMeters(actualPoint.y);
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

        if (project.fusionnedSurfaces != null) {
            for (FusionnedSurfaceDto fsDto: project.fusionnedSurfaces) {
                List<SurfaceUI> surfaceUIS = fsDto.fusionnedSurfaces.stream().map(surfaceDto -> new RectangleSurfaceUI(surfaceDto,
                        zoomManager,
                        selectionManager,
                        drawingSection,
                        snapGridUI,
                        this.tileInfo)).collect(Collectors.toList());
                FusionedSurfaceUI fsUI = new FusionedSurfaceUI(zoomManager, selectionManager, drawingSection, snapGridUI, surfaceUIS);
                this.allSurfaces.add(fsUI);
            }
        }
    }

    private void clearDrawings() {
        this.allSurfaces.forEach(SurfaceUI::hide);
        this.selectionManager.unselectAll();
        this.allSurfaces.clear();
        this.snapGridUI.renderForViewBox(this.getViewBoxSummits());
    }

    private void displaySurface(SurfaceDto surfaceDto) {
        RectangleSurfaceUI surfaceUi = new RectangleSurfaceUI(surfaceDto,
                zoomManager,
                selectionManager,
                drawingSection,
                snapGridUI,
                this.tileInfo);
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

    public void surfaceHole(){
        List<SurfaceUI> selectedSurfaces = this.selectionManager.getSelectedSurfaces();

        for (SurfaceUI surface: selectedSurfaces) {
            surface.setHole(true);
            domainController.updateSurface(surface.toDto());
            surface.hideTiles();
        }
        hideRectangleInfo();
        renderFromProject();
    }
    //PHIL A FAIT CETTE MÉTHODE HAHA XD
    //TODO void ou pas void ?
    public void createNewMaterial(){

        MaterialUI newMaterialUI = new MaterialUI();

        newMaterialUI.name = materialNameInputBox.getText();
        //TODO on a pas le nombre de botes
        newMaterialUI.numberOfBoxes = materialNumberOfBoxInputColumn.getText();
        newMaterialUI.tilePerBox = tilePerBoxInputBox.getText();
        newMaterialUI.color = materialColorChoiceBox.getValue();
        newMaterialUI.pricePerBoxe = boxPriceInputBox.getText();
        //TODO à coder mais on a pas le nombre de tuiles
        newMaterialUI.totalPrice = materialTotalPriceColumn.getText();

        materialTableView.getItems().add(newMaterialUI);
        tileMaterialChoiceBox.getItems().add(materialNameInputBox.getText());

        MaterialDto dto = new MaterialDto();
        dto.materialType = MaterialType.tileMaterial;
        dto.name = materialNameInputBox.getText();
        if(materialColorChoiceBox.getValue() == "BLACK"){
            dto.color = Color.BLACK;

        }else if(materialColorChoiceBox.getValue() == "WHITE"){
            dto.color = Color.WHITE;

        }else if(materialColorChoiceBox.getValue() == "YELLOW"){
            dto.color = Color.YELLOW;

        }else if(materialColorChoiceBox.getValue() == "GREEN"){
            dto.color = Color.GREEN;

        }else if(materialColorChoiceBox.getValue() == "BLUE"){
            dto.color = Color.BLUE;

        }else if(materialColorChoiceBox.getValue() == "RED"){
            dto.color = Color.RED;

        }else if(materialColorChoiceBox.getValue() == "VIOLET"){
            dto.color = Color.VIOLET;

        }else{
            throw new RuntimeException("Les couleurs petent mon gars");
        }
//        try{
//
//        }catch (ParseException e ) {
//            System.out.println("STFU ça pete");
//            afficherRectangleInfo();
//
//        }
        domainController.createMaterial(dto);



    }
    private void creatmaterialTest(){
        MaterialUI dtoFuckShit = new MaterialUI();

        dtoFuckShit.name = "Bois";
        dtoFuckShit.numberOfBoxes = "5";
        dtoFuckShit.tilePerBox = "6";
        dtoFuckShit.color = "GREEN";
        dtoFuckShit.pricePerBoxe = "6";
        dtoFuckShit.totalPrice = "69";

        materialTableView.getItems().add(dtoFuckShit);
    }
}
