package gui;

import application.Controller;
import application.SurfaceDto;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import utils.*;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class RectangleSurfaceUI implements SurfaceUI {

    private Id id;
    private Rectangle rectangle;
    private boolean isSelected = false;
    private List<AttachmentPointUI> attachmentPoints = new LinkedList<>();
    private Pane parentNode;
    private Controller controller = Controller.getInstance();
    private ZoomManager zoomManager;

    public RectangleSurfaceUI(SurfaceDto surfaceDto,
                              ZoomManager zoomManager,
                              SelectionManager selectionManager,
                              Pane parentNode) {

        this.id = surfaceDto.id;

        RectangleInfo rectangleInfo = RectangleHelper.summitsToRectangleInfo(surfaceDto.summits);

        Point topLeftCorner = zoomManager.metersToPixels(rectangleInfo.topLeftCorner);
        double width = zoomManager.metersToPixels(rectangleInfo.width);
        double height = zoomManager.metersToPixels(rectangleInfo.height);

        rectangle = new Rectangle(topLeftCorner.x, topLeftCorner.y, width, height);
        rectangle.setFill(Color.WHITE);
        rectangle.setStroke(Color.BLACK);

        this.parentNode = parentNode;
        this.zoomManager = zoomManager;

        RectangleSurfaceUI that = this;
        rectangle.setOnMouseClicked(new EventHandler<MouseEvent>()
        {
            @Override
            public void handle(MouseEvent t) {
                selectionManager.selectSurface(that);
                t.consume();
            }
        });

        rectangle.setOnMouseDragged(new EventHandler<MouseEvent>()
        {
            @Override
            public void handle(MouseEvent t) {
                hideAttachmentPoints();
                rectangle.setX(t.getX() - (rectangle.getWidth() / 2));
                rectangle.setY(t.getY() - (rectangle.getHeight() / 2));
                t.consume();

                controller.updateSurface(that.toDto());
            }
        });

        rectangle.setOnMouseEntered(new EventHandler<MouseEvent>()
        {
            @Override
            public void handle(MouseEvent t) {
                rectangle.setCursor(Cursor.HAND);
            }
        });

        rectangle.setOnMouseExited(new EventHandler<MouseEvent>()
        {
            @Override
            public void handle(MouseEvent t) {
                rectangle.setCursor(Cursor.DEFAULT);
            }
        });
    }

    public Node getNode() {
        return rectangle;
    }

    public void select() {
        isSelected = true;
        if (attachmentPoints.isEmpty()) {
            displayAttachmentPoints();
        }
    }

    public void unselect() {
        isSelected = false;
        hideAttachmentPoints();
    }

    public void increaseSizeBy(double deltaWidth, double deltaHeight) {
        double newWidth = rectangle.getWidth() + deltaWidth;
        double newHeight = rectangle.getHeight() + deltaHeight;

        if (newWidth >=0) {
            rectangle.setWidth(newWidth);
        }
        if (newHeight >= 0) {
            rectangle.setHeight(newHeight);
        }

        controller.updateSurface(this.toDto());
    }

    public SurfaceDto toDto() {
        SurfaceDto dto = new SurfaceDto();

        dto.summits = this.getSummits().stream().map(p -> zoomManager.pixelsToMeters(p)).collect(Collectors.toList());
        dto.isRectangular = true;
        dto.id = this.id;

        return dto;
    }

    private void displayAttachmentPoints() {
        List<Point> summits = this.getSummits();

        for (Point summit: summits) {
            attachmentPoints.add(new AttachmentPointUI(summit, summit.cardinality, this));
        }

        parentNode.getChildren().addAll(attachmentPoints.stream().map(AttachmentPointUI::getNode).collect(Collectors.toList()));
    }

    private List<Point> getSummits() {
        Point topLeft = new Point(rectangle.getX(), rectangle.getY());
        return RectangleHelper.rectangleInfoToSummits(topLeft, rectangle.getWidth(), rectangle.getHeight());
    }

    private void hideAttachmentPoints() {
        parentNode.getChildren().removeAll(attachmentPoints.stream().map(AttachmentPointUI::getNode).collect(Collectors.toList()));
        attachmentPoints.clear();
    }
}