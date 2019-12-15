package gui;

import Domain.HoleStatus;
import application.*;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import utils.*;

import java.util.List;
import java.util.stream.Collectors;

public class RectangleSurfaceUI extends SurfaceUI implements BoundingBoxResizable {

    public RectangleSurfaceUI(SurfaceDto surfaceDto,
                              ZoomManager zoomManager,
                              SelectionManager selectionManager,
                              SnapGridUI snapGrid,
                              Label tileInfoTextField
    ) {

        super(surfaceDto, zoomManager, selectionManager, snapGrid, tileInfoTextField);

        RectangleInfo rectangleInfo = RectangleHelper.summitsToRectangleInfo(surfaceDto.summits);

        Point topLeftCorner = zoomManager.metersToPixels(rectangleInfo.topLeftCorner);
        double width = zoomManager.metersToPixels(rectangleInfo.width);
        double height = zoomManager.metersToPixels(rectangleInfo.height);

        shape = new Rectangle(topLeftCorner.x, topLeftCorner.y, width, height);
        summits = this.getSummits();

        super.surfaceGroup.getChildren().add(shape);
        surfaceGroup.setCursor(Cursor.HAND);

        this.updateColor();

        this.renderTiles(surfaceDto.tiles);

        initializeGroup();
    }

    @Override
    protected void handleSurfaceDrag(MouseEvent event) {
        hideAttachmentPoints();
        hideResizeIndicator();
        hideTiles();

        double newX = event.getX() - this.lastPointOfContactRelativeToSurface.x;
        double newY = event.getY() - this.lastPointOfContactRelativeToSurface.y;

        Rectangle rectangle = (Rectangle) shape;
        rectangle.setX(newX);
        rectangle.setY(newY);
        summits = this.getSummits();

        this.updateColor(true);
        event.consume();
    }

    @Override
    protected Point getPixelPosition() {
        Rectangle rectangle = (Rectangle) shape;
        return new Point(rectangle.getX(), rectangle.getY());
    }

    @Override
    protected void snapToGrid() {
        if (this.snapGrid.isVisible()) {

            Rectangle rectangle = (Rectangle) shape;
            Point currentRectanglePosition = new Point(rectangle.getX(), rectangle.getY());
            Point nearestGridPoint = this.snapGrid.getNearestGridPoint(currentRectanglePosition);
            rectangle.setX(nearestGridPoint.x);
            rectangle.setY(nearestGridPoint.y);
            summits = this.getSummits();
        }
    }

    public void increaseSizeBy(double deltaWidth, double deltaHeight) {
        Rectangle rectangle = (Rectangle) shape;
        double newWidth = rectangle.getWidth() + deltaWidth;
        double newHeight = rectangle.getHeight() + deltaHeight;

        hideAttachmentPoints();
        hideTiles();
        updateColor(true);

        if (newWidth >= 0) {
            rectangle.setWidth(newWidth);
        }
        if (newHeight >= 0) {
            rectangle.setHeight(newHeight);
        }
        summits = this.getSummits();
    }

    public SurfaceDto toDto() {
        SurfaceDto dto = new SurfaceDto();

        dto.summits = this.summits.stream().map(p -> zoomManager.pixelsToMeters(p)).collect(Collectors.toList());
        dto.isRectangular = true;
        dto.id = this.id;
        dto.isHole = this.isHole;
        dto.masterTile = super.masterTile;
        dto.pattern = super.pattern;
        dto.surfaceColor = super.surfaceColor;
        dto.tileAngle = super.tileAngle;
        dto.tileShifting = super.tileShifting;

        if (this.isHole == HoleStatus.FILLED && this.tiles != null && this.tiles.size() != 0) {
            dto.tiles = this.tiles.stream().map(r -> r.toDto()).collect(Collectors.toList());
        }

        return dto;
    }

    private List<Point> getSummits() {
        Rectangle rectangle = (Rectangle) shape;
        Point topLeft = new Point(rectangle.getX(), rectangle.getY());
        return RectangleHelper.rectangleInfoToSummits(topLeft, rectangle.getWidth(), rectangle.getHeight());
    }

    public void setSize(double width, double height) {
        double pixelWidth = zoomManager.metersToPixels(width);
        double pixelHeight = zoomManager.metersToPixels(height);

        Rectangle rectangle = (Rectangle) shape;
        rectangle.setWidth(pixelWidth);
        rectangle.setHeight(pixelHeight);
        summits = this.getSummits();
    }

    public void setPosition(Point position) {

        Point topLeftCorner = zoomManager.metersToPixels(position);

        Rectangle rectangle = (Rectangle) shape;
        rectangle.setX(topLeftCorner.x);
        rectangle.setY(topLeftCorner.y);
        summits = this.getSummits();
    }


    public Shape getMainShape() {
        return this.shape;
    }

    public void translatePixelBy(Point translation) {
        Rectangle rectangle = (Rectangle) shape;
        rectangle.setX(rectangle.getX() + translation.x);
        rectangle.setY(rectangle.getY() + translation.y);
        summits = this.getSummits();
    }

    @Override
    public void resizeRespectingBoundingBox(Point topLeftBounding, Point bottomRightBounding, double deltaWidth, double deltaHeight) {
        List<Point> summits = this.summits.stream().map(s -> {
            double xImpact = (s.x - topLeftBounding.x) / (bottomRightBounding.x - topLeftBounding.x);
            double yImpact = (s.y - topLeftBounding.y) / (bottomRightBounding.y - topLeftBounding.y);

            return s.translate(new Point(deltaWidth * xImpact, deltaHeight * yImpact));
        }).collect(Collectors.toList());

        AbstractShape shape = new AbstractShape(summits);
        double width = ShapeHelper.getWidth(shape);
        double Height = ShapeHelper.getHeight(shape);
        Point topLeft = ShapeHelper.getTopLeftCorner(shape);

        Rectangle rect = (Rectangle) this.shape;
        rect.setX(topLeft.x);
        rect.setY(topLeft.y);
        rect.setWidth(width);
        rect.setHeight(Height);

        this.summits = this.getSummits();
    }
}