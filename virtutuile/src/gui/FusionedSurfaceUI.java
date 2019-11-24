package gui;

import application.Controller;
import application.SealsInfoDto;
import application.SurfaceDto;
import application.TileDto;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import utils.Id;
import utils.Point;
import utils.RectangleHelper;
import utils.RectangleInfo;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class FusionedSurfaceUI implements SurfaceUI {

    private Id id;
    private List<Rectangle> tiles;
    private boolean isHole;
    private List<Point> summits;

    private Group fusionedSurfaceGroup;
    private Shape shape;

    private List<AttachmentPointUI> attachmentPoints = new LinkedList<>();

    private Controller controller = Controller.getInstance();
    private ZoomManager zoomManager;
    private SelectionManager selectionManager;
    private SnapGridUI snapGrid;

    private Point lastPointOfContact = new Point(0,0);
    private Point position;
    private boolean currentlyBeingDragged = false;

    private TileDto masterTile;
    private SealsInfoDto sealsInfo;

    private List<SurfaceUI> allSurfacesToFusion;

    private SurfaceDto surfaceDto;

    private void renderShapeFromChilds() {
        this.shape = allSurfacesToFusion.get(0).getMainShape();

        allSurfacesToFusion.forEach(s -> {
            s.hide();

            if (s.getMainShape() == this.shape) {
                return;
            }
            this.shape = Shape.union(this.shape, s.getMainShape());
        });
        this.fusionedSurfaceGroup.getChildren().add(this.shape);

        this.summits = surfaceDto.summits.stream().map(s -> zoomManager.metersToPixels(s)).collect(Collectors.toList());
        double minX = Collections.min(this.summits.stream().map(s -> s.x).collect(Collectors.toList()));
        this.position = Collections.min(this.summits.stream().filter(s -> s.x == minX).collect(Collectors.toList()), Comparator.comparing(s -> s.y));
    }

    public FusionedSurfaceUI(ZoomManager zoomManager,
                             SelectionManager selectionManager,
                             SnapGridUI snapGrid,
                             SurfaceDto surfaceDto
                             ) {

        this.allSurfacesToFusion
                = surfaceDto
                .fusionnedSurface
                .stream()
                .map(fs -> new RectangleSurfaceUI(fs, zoomManager, selectionManager, snapGrid, new Label()))
                .collect(Collectors.toList());

        this.surfaceDto = surfaceDto;
        this.zoomManager = zoomManager;

        this.fusionedSurfaceGroup = new Group();
        this.renderShapeFromChilds();

        this.shape.setFill(Color.WHITE);
        this.shape.setStroke(Color.BLACK);

        this.id = surfaceDto.id;
        this.snapGrid = snapGrid;

        fusionedSurfaceGroup.setCursor(Cursor.HAND);

        this.zoomManager = zoomManager;
        this.selectionManager = selectionManager;
        this.isHole = false;

        initializeGroup();
    }

    private void initializeGroup(){
       fusionedSurfaceGroup.setOnMouseClicked(t -> {
           selectionManager.selectFusionnedSurface(this);
           t.consume();
       });

       fusionedSurfaceGroup.setOnMousePressed(mouseEvent -> {
           this.lastPointOfContact = new Point(mouseEvent.getX() - this.position.x, mouseEvent.getY() - this.position.y);
       });

       fusionedSurfaceGroup.setOnMouseReleased(mouseEvent -> {
           if(this.currentlyBeingDragged){
               this.currentlyBeingDragged = false;
               this.snapToGrid();
           }
       });

       fusionedSurfaceGroup.setOnMouseDragged(mouseEvent -> {
           hideAttachmentPoints();
           hideTiles();

           this.currentlyBeingDragged = true;

           double newX = mouseEvent.getX() - this.lastPointOfContact.x;
           double newY = mouseEvent.getY() - this.lastPointOfContact.y;
           Point newPoint = new Point(newX, newY);

           this.setPosition(newPoint);

           mouseEvent.consume();
       });
    }

    private void snapToGrid() {
        if(this.snapGrid.isVisible()){
            Point currentFusionedSurfacePosition  = new Point(this.position.x, this.position.y);
            Point nearestGridPoint = this.snapGrid.getNearestGridPoint(currentFusionedSurfacePosition);
            this.position.x = nearestGridPoint.x;
            this.position.y = nearestGridPoint.y;

            this.controller.updateSurface(this.toDto());
        }
    }

    @Override
    public Node getNode() {
        return fusionedSurfaceGroup;
    }

    @Override
    public Shape getMainShape() {
        return this.shape;
    }

    @Override
    public SurfaceDto toDto() {
        SurfaceDto dto = new SurfaceDto();

        // TODO: bad fucking abstraction

        return dto;
    }

    @Override
    public void select() {
        if(attachmentPoints.isEmpty()){
            displayAttachmentPoints();
        }
    }

    private void displayAttachmentPoints(){
        for(Point summit: summits){
            attachmentPoints.add(new AttachmentPointUI(summit, this));
        }
        fusionedSurfaceGroup.getChildren().addAll(attachmentPoints.stream().map(AttachmentPointUI::getNode).collect(Collectors.toList()));
    }

    private void hideAttachmentPoints(){
        fusionedSurfaceGroup.getChildren().removeAll(attachmentPoints.stream().map(AttachmentPointUI::getNode).collect(Collectors.toList()));
        attachmentPoints.clear();
    }

    @Override
    public void unselect() {
        hideAttachmentPoints();
    }

    @Override
    public Id getId() { return id; }

    @Override
    public void delete() {
        hide();
        controller.removeSurface(this.toDto());
    }

    @Override
    public void setMasterTile(TileDto newMasterTile) { this.masterTile = newMasterTile; }

    @Override
    public TileDto getMasterTile() { return this.masterTile; }

    @Override
    public void setSealsInfo(SealsInfoDto newSealInfos) { this.sealsInfo = newSealInfos; }

    @Override
    public SealsInfoDto getSealsInfo() {return this.sealsInfo; }

    @Override
    public void hideTiles() {
        if(this.tiles != null){
            this.fusionedSurfaceGroup.getChildren().removeIf(this.tiles::contains);
            this.tiles.clear();
        }
    }

    @Override
    public void hide() {
        this.unselect();
        this.hideTiles();
    }

    @Override
    public void fill() {
        this.renderTiles(controller.fillSurface(this.toDto(), this.masterTile, null, this.sealsInfo));
    }

    @Override
    public void setSize(double width, double height) {
        double pixelWidth = zoomManager.metersToPixels(width);
        double pixelHeight = zoomManager.metersToPixels(height);
    }

    /*public void increaseSizeBy(double deltaWidth, double deltaHeight){
        //double newWidth = shape.getWidth() + delatWidth;
        //double newHeight = shape.getWidth() + deltaHeight

        hideTiles();

        //if (newWidth >=0) {
            //shape.setWidth(newWidth);
        //}
        //if (newHeight >= 0) {
            //shape.setHeight(newHeight);
        //}

        controller.updateSurface(this.toDto());*//*
    }*/

    @Override
    public void setPosition(Point position) {
        Point pixels = zoomManager.metersToPixels(position);
        Point translation = Point.diff(pixels, this.position);

        System.out.println(String.format("click : (%f, %f)", position.x, position.y));

        allSurfacesToFusion.forEach(s -> s.translateBy(translation));

        this.fusionedSurfaceGroup.getChildren().remove(this.shape);
        this.renderShapeFromChilds();
    }

    public void translateBy(Point translation) {

    }

    @Override
    public void setHole(boolean isHole) { this.isHole = isHole; }

    private void renderTiles(List<TileDto> tiles) {
        if(this.isHole || tiles == null || tiles.size() == 0){
            return;
        }

        List<RectangleInfo>  tilesRect = tiles.stream().map(t -> {
            List<Point> pixelPoints = t.summits.stream().map(zoomManager::metersToPixels).collect(Collectors.toList());
            return RectangleHelper.summitsToRectangleInfo(pixelPoints);
        }).collect(Collectors.toList());

        hideTiles();

        this.tiles = tilesRect.stream().map(t -> {
            Rectangle tileUi = new Rectangle(t.topLeftCorner.x, t.topLeftCorner.y, t.width, t.height);
            tileUi.setFill(Color.PALETURQUOISE);
            tileUi.setStroke(Color.DARKTURQUOISE);

            tileUi.setOnMouseEntered(event -> tileUi.setFill(Color.PALEGOLDENROD));
            tileUi.setOnMouseExited(event -> tileUi.setFill(Color.PALETURQUOISE));

            return tileUi;
        }).collect(Collectors.toList());
        this.fusionedSurfaceGroup.getChildren().addAll(this.tiles);
    }

    public void forceFill() {

    }
}
