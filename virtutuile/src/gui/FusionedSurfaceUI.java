package gui;

import Domain.HoleStatus;
import application.Controller;
import application.SurfaceDto;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;
import utils.*;

import java.util.*;
import java.util.stream.Collectors;

public class FusionedSurfaceUI extends SurfaceUI {

    private Shape shape;

    private Point lastPointOfContact = new Point(0,0);
    private Point position;
    private boolean currentlyBeingDragged = false;

    private List<SurfaceUI> allSurfacesToFusion;

    public FusionedSurfaceUI(SurfaceDto surfaceDto,
                             ZoomManager zoomManager,
                             SelectionManager selectionManager,
                             SnapGridUI snapGrid,
                             Label tileInfoLabel
                             ) {

        super(surfaceDto, zoomManager, selectionManager, snapGrid, tileInfoLabel);

        this.allSurfacesToFusion
                = surfaceDto
                .fusionnedSurface
                .stream()
                .map(fs -> {
                    if (fs.isRectangular) {
                        return new RectangleSurfaceUI(fs, zoomManager, selectionManager, snapGrid, tileInfoLabel);
                    }
                    return new IrregularSurfaceUI(fs, zoomManager, selectionManager, snapGrid, tileInfoLabel);
                })
                .collect(Collectors.toList());

        this.zoomManager = zoomManager;

        super.surfaceGroup = new Group();
        this.renderShapeFromChilds();

        this.snapGrid = snapGrid;

        surfaceGroup.setCursor(Cursor.HAND);

        this.setShapeColor();

        this.renderTiles(surfaceDto.tiles);

        initializeGroup();
    }

    private void renderShapeFromChilds() {
        allSurfacesToFusion = sortList(allSurfacesToFusion);
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

        this.setShapeColor();
        super.surfaceGroup.getChildren().add(this.shape);

        List<AbstractShape> allSurfacesSummits = allSurfacesToFusion
                .stream()
                .map(s -> new AbstractShape(s.toDto().summits
                        .stream()
                        .map(su -> zoomManager.metersToPixels(su))
                        .collect(Collectors.toList()), s.toDto().isHole == HoleStatus.HOLE))
                .collect(Collectors.toList());

        super.summits = FusionHelper.getFusionResultSummits(allSurfacesSummits).summits;
        double minX = Collections.min(super.summits.stream().map(s -> s.x).collect(Collectors.toList()));
        this.position = Collections.min(super.summits.stream().filter(s -> s.x == minX).collect(Collectors.toList()), Comparator.comparing(s -> s.y));

//        System.out.println(String.format("(%f, %f)", this.position.x, this.position.y));
    }

    private void setShapeColor() {
        if (sealsInfo != null) {
            shape.setFill(ColorHelper.utilsColorToJavafx(sealsInfo.color));
            shape.setStroke(Color.BLACK);
            return;
        }
        shape.setFill(Color.WHITE);
        shape.setStroke(Color.BLACK);
    }

    private void initializeGroup(){
       surfaceGroup.setOnMouseClicked(t -> {
           selectionManager.selectSurface(this);
           t.consume();
       });

       surfaceGroup.setOnMousePressed(mouseEvent -> {
           this.lastPointOfContact = new Point(mouseEvent.getX() - this.position.x, mouseEvent.getY() - this.position.y);
       });

       surfaceGroup.setOnMouseReleased(mouseEvent -> {
           if(this.currentlyBeingDragged){
               this.currentlyBeingDragged = false;
               this.snapToGrid();

               if (this.isHole != HoleStatus.FILLED || this.tiles == null) {
                   controller.updateSurface(this.toDto());
                   return;
               }
               this.renderTiles(controller.updateAndRefill(this.toDto(), this.masterTile, null, this.sealsInfo));
           }
       });

       surfaceGroup.setOnMouseDragged(mouseEvent -> {
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
        dto.isHole = this.isHole;
        dto.id = this.id;

        dto.masterTile = this.masterTile;
        dto.pattern = super.pattern;

        if (this.isHole == HoleStatus.FILLED && this.tiles != null && this.tiles.size() != 0) {
            dto.tiles = this.tiles.stream().map(r -> r.toDto()).collect(Collectors.toList());
        }

        return dto;
    }


    @Override
    public void fill() {
        this.renderTiles(controller.fillSurface(this.toDto(), this.masterTile, null, this.sealsInfo));
        setShapeColor();
    }

    @Override
    public void setSize(double width, double height) {}

    @Override
    public void setPosition(Point position) {
        this.setPixelPosition(zoomManager.metersToPixels(position));
    }

    private void setPixelPosition(Point position) {

        Point translation = Point.diff(position, this.position);

        allSurfacesToFusion.forEach(s -> s.translatePixelBy(translation));

        super.surfaceGroup.getChildren().remove(this.shape);
        this.renderShapeFromChilds();
    }

    public void translatePixelBy(Point translation) {
        this.setPixelPosition(new Point(this.position.x + translation.x, this.position.y + translation.y));
    }

    @Override
    public void setHole(HoleStatus isHole) {
        if (isHole != HoleStatus.HOLE) { // Can't be a hole...
            this.isHole = isHole;
        }
    }

    private static List<SurfaceUI> sortList(List<SurfaceUI> surfaces) {

        List<SurfaceUI> returnedList = new ArrayList<>();
        List<SurfaceUI> otherHoleList = new ArrayList<>();

        for (SurfaceUI s : surfaces) {
            if (s.toDto().isHole != HoleStatus.HOLE) {
                returnedList.add(s);
            } else {
                otherHoleList.add(s);
            }
        }

        returnedList.addAll(otherHoleList);
        return returnedList;
    }
}
