package gui;

import Domain.HoleStatus;
import application.SurfaceDto;
import gui.sidepanel.TileInfoUI;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Shape;
import utils.AbstractShape;
import utils.Point;
import utils.ShapeHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class IrregularSurfaceUI extends SurfaceUI implements BoundingBoxResizable {

    private boolean currentlyBeingDragged = false;

    public IrregularSurfaceUI(SurfaceDto surfaceDto,
                              ZoomManager zoomManager,
                              SelectionManager selectionManager,
                              SnapGridUI snapGrid,
                              TileInfoUI tileInfoTextField) {
        super(surfaceDto, zoomManager, selectionManager, snapGrid, tileInfoTextField);

        renderRectangleFromSummits(surfaceDto.summits.stream().map(s -> zoomManager.metersToPixels(s)).collect(Collectors.toList()));
        updateColor();

        summits = this.getSummits();

        this.renderTiles(surfaceDto.tiles);

        initializeGroup();
    }

    private void renderRectangleFromSummits(List<Point> newSummits) {
        super.surfaceGroup.getChildren().remove(shape);

        List<Double> allNumbers = new ArrayList<>();
        newSummits.stream().forEach(s -> {
            allNumbers.add(s.x);
            allNumbers.add(s.y);
        });

        shape = new Polygon();
        ((Polygon) shape).getPoints().addAll(allNumbers);

        shape.setCursor(Cursor.HAND);

        super.surfaceGroup.getChildren().add(shape);
        this.updateColor(this.currentlyBeingDragged);
    }


    @Override
    protected void handleSurfaceDrag(MouseEvent event) {
        hideAttachmentPoints();
        hideResizeIndicator();
        hideTiles();

        this.currentlyBeingDragged = true;

        double newX = event.getX() - this.lastPointOfContactRelativeToSurface.x;
        double newY = event.getY() - this.lastPointOfContactRelativeToSurface.y;

//            System.out.println(String.format("(%f, %f)", getPosition().x, getPosition().y));

        Point translation = Point.diff(new Point(newX, newY), getPixelPosition());
        this.translatePixelBy(translation);

        this.updateColor(true);
        event.consume();
    }

    @Override
    protected void snapToGrid() {
        if (super.snapGrid.isVisible()) {
            Point currentRectanglePosition = new Point(getPixelPosition().x, getPixelPosition().y);
            Point nearestGridPoint = this.snapGrid.getNearestGridPoint(currentRectanglePosition);

            Point position = getPixelPosition();
            Point translation = Point.diff(new Point(nearestGridPoint.x, nearestGridPoint.y), position);

//            System.out.println(String.format("position: (%.1f, %.1f), translation: (%.1f, %.1f)", position.x, position.y, translation.x, translation.y));

            this.renderRectangleFromSummits(this.summits.stream().map(s -> s.translate(translation)).collect(Collectors.toList()));

            summits = this.getSummits();
        }
    }

    private List<Point> getSummits() {
        List<Double> coords = ((Polygon) shape).getPoints();
        List<Point> returned = new ArrayList<>();

        int index = 0;
        for (Double coord : coords) {
            if (index % 2 == 1) {
                Double x = coords.get(index - 1);
                Double y = coords.get(index);
                returned.add(new Point(x, y));
            }
            index++;
        }
        return returned;
    }

    @Override
    protected Point getPixelPosition() {
        AbstractShape shape = new AbstractShape(this.getSummits(), false);
        return ShapeHelper.getTopLeftCorner(shape);
    }

    @Override
    public Shape getMainShape() {
        return shape;
    }

    @Override
    public SurfaceDto toDto() {
        SurfaceDto dto = new SurfaceDto();

        dto.summits = this.summits.stream().map(p -> zoomManager.pixelsToMeters(p)).collect(Collectors.toList());
        dto.isRectangular = false;
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

    @Override
    public void setSize(double width, double height) {

        double pixelWidth = zoomManager.metersToPixels(width);

        AbstractShape shape = new AbstractShape(this.getSummits());

        double currentWidth = ShapeHelper.getWidth(shape);
        double currentHeight = ShapeHelper.getHeight(shape);
        double widthToHeightRatio = currentHeight / currentWidth;

        double deltaWidth = pixelWidth - currentWidth;

        double newHeight = pixelWidth * widthToHeightRatio;
        double deltaHeight = newHeight - currentHeight;

        increaseSizeBy(deltaWidth, deltaHeight);
    }

    @Override
    public void setPosition(Point position) {
        Point pixelPosition = zoomManager.metersToPixels(position);

        Point translation = Point.diff(pixelPosition, getPixelPosition());
        translatePixelBy(translation);
    }

    @Override
    public void translatePixelBy(Point translation) {
        this.renderRectangleFromSummits(this.summits.stream().map(s -> s.translate(translation)).collect(Collectors.toList()));
        summits = this.getSummits();
    }

    @Override
    public void increaseSizeBy(double deltaWidth, double deltaHeight) {
        hideAttachmentPoints();
        hideTiles();

        List<Point> summits = getSummits();
        AbstractShape shape = new AbstractShape(summits);
        Point boundingTopLeft = ShapeHelper.getTheoricalTopLeftCorner(shape);
        Point boundingBottomRight = ShapeHelper.getTheoricalBottomRightCorner(shape);

        if (boundingBottomRight.x + deltaWidth <= boundingTopLeft.x
                || boundingBottomRight.y + deltaHeight <= boundingTopLeft.y) {
            return;
        }

        resizeRespectingBoundingBox(boundingTopLeft, boundingBottomRight, deltaWidth, deltaHeight);
    }

    @Override
    public void resizeRespectingBoundingBox(Point topLeftBounding, Point bottomRightBounding, double deltaWidth, double deltaHeight) {
        summits = summits.stream().map(s -> {
            double xImpact = (s.x - topLeftBounding.x) / (bottomRightBounding.x - topLeftBounding.x);
            double yImpact = (s.y - topLeftBounding.y) / (bottomRightBounding.y - topLeftBounding.y);

            return s.translate(new Point(deltaWidth * xImpact, deltaHeight * yImpact));
        }).collect(Collectors.toList());

        renderRectangleFromSummits(summits);
        this.summits = getSummits();
    }
}
