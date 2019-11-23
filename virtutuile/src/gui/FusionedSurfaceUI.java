package gui;

import Domain.Surface;
import application.Controller;
import application.SealsInfoDto;
import application.SurfaceDto;
import application.TileDto;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import utils.Id;
import utils.Point;
import utils.RectangleHelper;
import utils.RectangleInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class FusionedSurfaceUI implements SurfaceUI {

    private Id id;
    private List<Rectangle> tiles;
    private boolean isHole;
    private List<Point> summits = new ArrayList<>();

    private Group fusionedSurfaceGroup;
    private Shape shape;

    private List<AttachmentPointUI> attachmentPoints = new LinkedList<>();
    private Pane parentNode;
    private Controller controller = Controller.getInstance();
    private ZoomManager zoomManager;
    private SelectionManager selectionManager;
    private SnapGridUI snapGrid;

    private Point lastPointOfContact = new Point(0,0);
    private Point firstSummit;
    private boolean currentlyBeingDragged = false;

    private TileDto masterTile;
    private SealsInfoDto sealsInfo;

    private List<SurfaceUI> initalSurfaces;

    public FusionedSurfaceUI(ZoomManager zoomManager,
                            SelectionManager selectionManager,
                            Pane parentNode,
                            SnapGridUI snapGrid,
                            List<SurfaceUI> allSurfacesToFusionne) {

        Shape firstShape = allSurfacesToFusionne.get(0).getMainShape();
        this.shape = firstShape;

        allSurfacesToFusionne.forEach(s -> {
            s.hide();

            if (s.getMainShape() == this.shape) {
                return;
            }
            this.shape = Shape.union(this.shape, s.getMainShape());
        });
        this.initalSurfaces = allSurfacesToFusionne;

        this.shape.setFill(firstShape.getFill());
        this.shape.setStroke(firstShape.getStroke());

        parentNode.getChildren().addAll(shape);

        List<Point> allSummits = new ArrayList<>();
        allSurfacesToFusionne.forEach(s ->{
            List<Point> listes = s.toDto().summits;
            listes.forEach(p ->{
                allSummits.add(zoomManager.metersToPixels(p));
            });
        });
        List<Double> x = new ArrayList<>();
        List<Double> y = new ArrayList<>();

        allSummits.forEach(s -> {
            x.add(s.x);
            y.add(s.y);
        });

        Double maxX = Collections.max(x);
        Double minX = Collections.min(x);
        Double maxY = Collections.max(y);
        Double minY = Collections.min(y);

        allSummits.forEach(r ->{
            if(r.x == maxX || r.x == minX || r.y == maxY || r.y == minY){
                summits.add(r);
            }
        });

        firstSummit = summits.get(0);

        this.id = allSurfacesToFusionne.get(0).getId();
        this.snapGrid = snapGrid;

        this.fusionedSurfaceGroup = new Group(this.shape);
        fusionedSurfaceGroup.setCursor(Cursor.HAND);

        this.parentNode = parentNode;
        this.zoomManager = zoomManager;
        this.selectionManager = selectionManager;
        this.isHole = false;

        //this.renderTiles(surfaceDto.tiles);

        initializeGroup();
        this.parentNode.getChildren().add(fusionedSurfaceGroup);
    }

    private void initializeGroup(){
       fusionedSurfaceGroup.setOnMouseClicked(t -> {
           selectionManager.selectFusionnedSurface(this);
           t.consume();
       });

       fusionedSurfaceGroup.setOnMousePressed(mouseEvent -> {
           this.lastPointOfContact = new Point(mouseEvent.getX() - this.firstSummit.x, mouseEvent.getY() - this.firstSummit.y);
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

           this.firstSummit = newPoint;

           mouseEvent.consume();

           controller.updateSurface(this.toDto());
       });
    }

    private void snapToGrid(){
        if(this.snapGrid.isVisible()){
            Point currentFusionedSurfacePosition  = new Point(this.firstSummit.x, this.firstSummit.y);
            Point nearestGridPoint = this.snapGrid.getNearestGridPoint(currentFusionedSurfacePosition);
            this.firstSummit.x = nearestGridPoint.x;
            this.firstSummit.y = nearestGridPoint.y;

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

        dto.summits = this.summits; // TODO: vincent, ca cest les summits en pixels, dans le dto on les veut en metre donc sert toi du zoomManager
        dto.isRectangular = false;
        dto.id = this.id;
        dto.isHole = this.isHole;

        if(!this.isHole && this.tiles != null && this.tiles.size() != 0){
            dto.tiles = this.tiles.stream().map(r -> {
                TileDto tile = new TileDto();
                tile.summits = RectangleHelper.rectangleInfoToSummits(r.getX(), r.getY(), r.getWidth(), r.getHeight());
                return tile;
            }).collect(Collectors.toList());
        }

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
        parentNode.getChildren().remove(this.getNode());

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
        Point firstSummit = zoomManager.metersToPixels(position);

        this.firstSummit.x = firstSummit.x;
        this.firstSummit.y = firstSummit.x;

    }

    @Override
    public void setHole(boolean isHole) { this.isHole = isHole; }

    private void renderTiles(List<TileDto> tiles){
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
}
