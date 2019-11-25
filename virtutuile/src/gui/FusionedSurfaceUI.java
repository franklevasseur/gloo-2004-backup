package gui;

import Domain.HoleStatus;
import application.Controller;
import application.SealsInfoDto;
import application.SurfaceDto;
import application.TileDto;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import utils.AbstractShape;
import utils.FusionHelper;
import utils.Id;
import utils.Point;

import java.util.*;
import java.util.stream.Collectors;

public class FusionedSurfaceUI implements SurfaceUI {

    private Id id;
    private List<Rectangle> tiles;
    private HoleStatus isHole;
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

    private void renderShapeFromChilds() {
        allSurfacesToFusion = sortFuckingStupidFuckingListDeTabarnakQueJavaCestUnEstiDeLangageDeCulVaChier(allSurfacesToFusion);
        SurfaceUI firstSurface = allSurfacesToFusion.get(0);
        this.shape = firstSurface.getMainShape();

        for (SurfaceUI s : allSurfacesToFusion) {
            s.hide();

            if (s == firstSurface) {
                continue;
            }
            if (s.toDto().isHole == HoleStatus.HOLE) {
                this.shape = Shape.subtract(this.shape, s.getMainShape());
                continue;
            }
            this.shape = Shape.union(this.shape, s.getMainShape());
        }

        this.setShapeAppearance();
        this.fusionedSurfaceGroup.getChildren().add(this.shape);

        List<AbstractShape> allSurfacesSummits = allSurfacesToFusion
                .stream()
                .map(s -> new AbstractShape(s.toDto().summits
                        .stream()
                        .map(su -> zoomManager.metersToPixels(su))
                        .collect(Collectors.toList()), s.toDto().isHole == HoleStatus.HOLE))
                .collect(Collectors.toList());

        this.summits = FusionHelper.getFusionResultSummits(allSurfacesSummits).summits;
        double minX = Collections.min(this.summits.stream().map(s -> s.x).collect(Collectors.toList()));
        this.position = Collections.min(this.summits.stream().filter(s -> s.x == minX).collect(Collectors.toList()), Comparator.comparing(s -> s.y));

//        System.out.println(String.format("(%f, %f)", this.position.x, this.position.y));
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

        this.zoomManager = zoomManager;

        this.fusionedSurfaceGroup = new Group();
        this.renderShapeFromChilds();

        this.setShapeAppearance();

        this.id = surfaceDto.id;
        this.snapGrid = snapGrid;

        fusionedSurfaceGroup.setCursor(Cursor.HAND);

        this.zoomManager = zoomManager;
        this.selectionManager = selectionManager;
        this.isHole = surfaceDto.isHole;

        initializeGroup();
    }

    private void setShapeAppearance() {
        this.shape.setFill(Color.WHITE);
        this.shape.setStroke(Color.BLACK);
    }

    private void initializeGroup(){
       fusionedSurfaceGroup.setOnMouseClicked(t -> {
           selectionManager.selectSurface(this);
           t.consume();
       });

       fusionedSurfaceGroup.setOnMousePressed(mouseEvent -> {
           this.lastPointOfContact = new Point(mouseEvent.getX() - this.position.x, mouseEvent.getY() - this.position.y);
       });

       fusionedSurfaceGroup.setOnMouseReleased(mouseEvent -> {
           if(this.currentlyBeingDragged){
               this.currentlyBeingDragged = false;
               this.snapToGrid();
               this.controller.updateSurface(this.toDto());
           }
       });

       fusionedSurfaceGroup.setOnMouseDragged(mouseEvent -> {
           hideAttachmentPoints();
           hideTiles();

           this.currentlyBeingDragged = true;

           double newX = mouseEvent.getX() - this.lastPointOfContact.x;
           double newY = mouseEvent.getY() - this.lastPointOfContact.y;
           Point newPoint = new Point(newX, newY);

           this.setPixelPosition(newPoint);

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

        dto.summits = this.summits.stream().map(su -> zoomManager.pixelsToMeters(su)).collect(Collectors.toList());
        dto.isFusionned = true;
        dto.isRectangular = false;
        dto.fusionnedSurface = this.allSurfacesToFusion.stream().map(s -> s.toDto()).collect(Collectors.toList());
        dto.isHole = HoleStatus.NONE;
//        dto.tiles = null;
        dto.id = this.id;

        return dto;
    }

    @Override
    public void select(boolean setToFront) {
        if(attachmentPoints.isEmpty()) {
            displayAttachmentPoints();
            if (setToFront) {
                this.fusionedSurfaceGroup.toFront();
            }
        }
    }

    private void displayAttachmentPoints(){
        for(Point summit: summits) {
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
    public Id getId() {
        return id;
    }

    @Override
    public void delete() {
        hide();
        controller.removeSurface(this.toDto());
    }

    @Override
    public void setMasterTile(TileDto newMasterTile) {
        this.masterTile = newMasterTile;
    }

    @Override
    public TileDto getMasterTile() {
        return this.masterTile;
    }

    @Override
    public void setSealsInfo(SealsInfoDto newSealInfos) {
        this.sealsInfo = newSealInfos;
    }

    @Override
    public SealsInfoDto getSealsInfo() {
        return this.sealsInfo;
    }

    @Override
    public void hideTiles() {}

    @Override
    public void hide() {}

    @Override
    public void fill() {}

    @Override
    public void setSize(double width, double height) {}

    @Override
    public void setPosition(Point position) {
        this.setPixelPosition(zoomManager.metersToPixels(position));
    }

    private void setPixelPosition(Point position) {

        Point translation = Point.diff(position, this.position);

//        System.out.println(String.format("(%.1f, %.1f)", translation.x, translation.y));
//        System.out.println(String.format("(%.1f, %.1f)", position.x, position.y));
//        System.out.println(String.format("(%.1f, %.1f)", this.position.x, this.position.y));

        allSurfacesToFusion.forEach(s -> s.translatePixelBy(translation));

//        String debug = "";
//        for (SurfaceUI s : allSurfacesToFusion) {
//            Rectangle rect = (Rectangle)s.getMainShape();
//            debug += String.format("(%.1f, %.1f) \n", rect.getX(), rect.getY());
//        }
//        System.out.println(debug);

        this.fusionedSurfaceGroup.getChildren().remove(this.shape);
        this.renderShapeFromChilds();
    }

    public void translatePixelBy(Point translation) {
        this.setPixelPosition(new Point(this.position.x + translation.x, this.position.y + translation.y));
    }

    @Override
    public void setHole(HoleStatus isHole) { this.isHole = isHole; }

    private void renderTiles(List<TileDto> tiles) {}

    public void forceFill() {}

    private static List<SurfaceUI> sortFuckingStupidFuckingListDeTabarnakQueJavaCestUnEstiDeLangageDeCulVaChier(List<SurfaceUI> surfaces) {

        List<SurfaceUI> laTabarnakDeCalisseDeListeQuonVaRetournerAFinDeLestiDeFonction = new ArrayList<>();
        List<SurfaceUI> lautreFuckingListeDeTrouEstiDeLaid = new ArrayList<>();

        for (SurfaceUI s : surfaces) {
            if (s.toDto().isHole != HoleStatus.HOLE) {
                laTabarnakDeCalisseDeListeQuonVaRetournerAFinDeLestiDeFonction.add(s);
            } else {
                lautreFuckingListeDeTrouEstiDeLaid.add(s);
            }
        }

        laTabarnakDeCalisseDeListeQuonVaRetournerAFinDeLestiDeFonction.addAll(lautreFuckingListeDeTrouEstiDeLaid);
        return laTabarnakDeCalisseDeListeQuonVaRetournerAFinDeLestiDeFonction;
    }
}
