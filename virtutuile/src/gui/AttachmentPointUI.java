package gui;

import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import utils.CardinalPoint;
import utils.Point;

public class AttachmentPointUI {

    private static final double pointWidth = 10;
    private Rectangle rectangle;

    private boolean currentlyBeingDragged = false;

    public AttachmentPointUI(Point coord, CardinalPoint cardinal, SurfaceUI parentSurface) {
        this(coord, cardinal, parentSurface, null);
    }

    public AttachmentPointUI(Point coord, CardinalPoint cardinal, SurfaceUI parentSurface, Cursor cursor) {
        rectangle = new Rectangle(coord.x - (pointWidth / 2), coord.y - (pointWidth / 2), pointWidth, pointWidth);

            if (cursor != null) {
                rectangle.setCursor(cursor);
            }
            else if (cardinal == CardinalPoint.SE) {
                rectangle.setCursor(Cursor.SE_RESIZE);
            }

        rectangle.setOnMouseDragged(t -> {
            if (cardinal != CardinalPoint.SE) {
                return;
            }

            currentlyBeingDragged = true;

            double deltaX = t.getX() - (rectangle.getX() + pointWidth / 2);
            double deltaY = t.getY() - (rectangle.getY() + pointWidth / 2);

            rectangle.setX(t.getX() - (pointWidth / 2));
            rectangle.setY(t.getY() - (pointWidth / 2));

            if (parentSurface.toDto().isRectangular) {
                ((RectangleSurfaceUI) parentSurface).increaseSizeBy(deltaX, deltaY);
            }
            t.consume();
        });

        rectangle.setOnMouseReleased(mouseEvent -> {
            if (currentlyBeingDragged) {
                currentlyBeingDragged = false;
                if (parentSurface.toDto().isRectangular) {
                    ((RectangleSurfaceUI) parentSurface).commitIncreaseSize();
                }
            }
        });

        rectangle.setOnMouseClicked(t -> {
            if (parentSurface != null) {
                parentSurface.unselect();
                parentSurface.select(false);
                t.consume();
            }
        });

        rectangle.setFill(Color.GRAY);
    }

    public Node getNode() {
        return rectangle;
    }

    public Point getPixelCoords() {
        // gives the middle of attachment point as its considered a point
        Point topLeftCorner = new Point(this.rectangle.getX(), this.rectangle.getY());
        double halfWidth = pointWidth / 2;
        return new Point(topLeftCorner.x + halfWidth, topLeftCorner.y + halfWidth);
    }
}
