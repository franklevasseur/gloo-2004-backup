package gui;

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

public class SurfaceUI {

    private Rectangle surfaceNode;

    private boolean isSelected = false;
    private List<AttachmentPointUI> attachmentPoints = new LinkedList<>();

    private Pane parentNode;

    public SurfaceUI(double x, double y, double width, double height, SelectionManager selectionManager, Pane parentNode) {
        surfaceNode = new Rectangle(x, y, width, height);
        this.parentNode = parentNode;

        SurfaceUI that = this;
        surfaceNode.setOnMouseClicked(new EventHandler<MouseEvent>()
        {
            @Override
            public void handle(MouseEvent t) {
                selectionManager.unselectAll();

                select();
                selectionManager.selectSurface(that);
                t.consume();
            }
        });

        surfaceNode.setOnMouseDragged(new EventHandler<MouseEvent>()
        {
            @Override
            public void handle(MouseEvent t) {
                if (isSelected) {
                    hideAttachmentPoints();
                    surfaceNode.setX(t.getX() - (width / 2));
                    surfaceNode.setY(t.getY() - (height / 2));
                    t.consume();
                }
            }
        });

        surfaceNode.setOnMouseEntered(new EventHandler<MouseEvent>()
        {
            @Override
            public void handle(MouseEvent t) {
                surfaceNode.setCursor(Cursor.HAND);
            }
        });

        surfaceNode.setOnMouseExited(new EventHandler<MouseEvent>()
        {
            @Override
            public void handle(MouseEvent t) {
                surfaceNode.setCursor(Cursor.DEFAULT);
            }
        });
    }

    public Node getNode() {
        return surfaceNode;
    }

    void select() {
        isSelected = true;
        surfaceNode.setFill(Color.RED);

        displayAttachmentPoints();
    }

    void unselect() {
        isSelected = false;
        surfaceNode.setFill(Color.BLACK);

        hideAttachmentPoints();
    }

    private void displayAttachmentPoints() {
        PixelPoint topLeft = new PixelPoint(surfaceNode.getX(), surfaceNode.getY());
        PixelPoint topRight = new PixelPoint(surfaceNode.getX() + surfaceNode.getWidth(), surfaceNode.getY());
        PixelPoint bottomLeft = new PixelPoint(surfaceNode.getX(), surfaceNode.getY() + surfaceNode.getHeight());
        PixelPoint bottomRight = new PixelPoint(surfaceNode.getX() + surfaceNode.getWidth(), surfaceNode.getY() + surfaceNode.getHeight());
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