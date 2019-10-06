package gui;

import application.Controller;
import domain.SurfaceId;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class RectangleSurfaceUI {

    private SurfaceId id;
    private Rectangle rectangle;
    private boolean isSelected = false;
    private List<AttachmentPointUI> attachmentPoints = new LinkedList<>();
    private Pane parentNode;
    private Controller controller = Controller.getInstance();

    public RectangleSurfaceUI(PixelPoint topLeftSummit,
                              double width,
                              double height,
                              SelectionManager selectionManager,
                              Pane parentNode) {
        rectangle = new Rectangle(topLeftSummit.x, topLeftSummit.y, width, height);
        rectangle.setFill(Color.WHITE);
        rectangle.setStroke(Color.BLACK);

        this.parentNode = parentNode;

        id = controller.createSurface(this);

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

                controller.updateSurface(that);
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

        if (newWidth >=0 ) {
            rectangle.setWidth(newWidth);
        }
        if (newHeight >= 0) {
            rectangle.setHeight(newHeight);
        }

        controller.updateSurface(this);
    }

    private void displayAttachmentPoints() {
        PixelPoint topLeft = new PixelPoint(rectangle.getX(), rectangle.getY());
        PixelPoint topRight = new PixelPoint(rectangle.getX() + rectangle.getWidth(), rectangle.getY());
        PixelPoint bottomLeft = new PixelPoint(rectangle.getX(), rectangle.getY() + rectangle.getHeight());
        PixelPoint bottomRight = new PixelPoint(rectangle.getX() + rectangle.getWidth(), rectangle.getY() + rectangle.getHeight());
        attachmentPoints.add(new AttachmentPointUI(topLeft, CardinalPoint.NW, this));
        attachmentPoints.add(new AttachmentPointUI(topRight, CardinalPoint.NE, this));
        attachmentPoints.add(new AttachmentPointUI(bottomLeft, CardinalPoint.SW, this));
        attachmentPoints.add(new AttachmentPointUI(bottomRight, CardinalPoint.SE, this));

        parentNode.getChildren().addAll(attachmentPoints.stream().map(AttachmentPointUI::getNode).collect(Collectors.toList()));
    }

    private void hideAttachmentPoints() {
        parentNode.getChildren().removeAll(attachmentPoints.stream().map(AttachmentPointUI::getNode).collect(Collectors.toList()));
        attachmentPoints.clear();
    }
}