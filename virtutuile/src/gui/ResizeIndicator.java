package gui;

import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import utils.Point;
import utils.Segment;

public class ResizeIndicator {

    private Circle node;
    private boolean currentlyBeingDragged = false;

    private Point initialCoord;

    public ResizeIndicator(Point pixelCoordinate, SurfaceUI parentSurface, boolean proportional) {
        node = new Circle(pixelCoordinate.x, pixelCoordinate.y, 5);
        node.setCursor(Cursor.SE_RESIZE);

        initialCoord = pixelCoordinate;

        node.setOnMouseDragged(t -> {
            currentlyBeingDragged = true;

            double newX = t.getX();
            double newY = proportional ?
                    new Segment(initialCoord, new Point(initialCoord.x + 100, initialCoord.y + 100)).predictY(newX)
                    : t.getY();

            double deltaX = newX - node.getCenterX();
            double deltaY = newY - node.getCenterY();

            node.setCenterX(newX);
            node.setCenterY(newY);

            if (parentSurface.toDto().isRectangular) {
                ((RectangleSurfaceUI) parentSurface).increaseSizeBy(deltaX, deltaY);
            }

            t.consume();
        });

        node.setOnMouseReleased(mouseEvent -> {
            if (currentlyBeingDragged) {
                currentlyBeingDragged = false;
                initialCoord = new Point(node.getCenterX(), node.getCenterY());
                if (parentSurface.toDto().isRectangular) {
                    ((RectangleSurfaceUI) parentSurface).commitIncreaseSize();
                }
            }
        });

        node.setOnMouseClicked(t -> {
            if (parentSurface != null) {
                parentSurface.unselect();
                parentSurface.select(false);
                t.consume();
            }
        });

        node.setFill(Color.CORNFLOWERBLUE);
    }

    public Node getNode() {
        return this.node;
    }
}
