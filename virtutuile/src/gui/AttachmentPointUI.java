package gui;

import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import utils.CardinalPoint;
import utils.Point;

public class AttachmentPointUI {

    private Rectangle rectangle;
    private double pointWidth;

    public AttachmentPointUI(Point coord, CardinalPoint cardinal, SurfaceUI parentSurface, double width) {
        this(coord, cardinal, parentSurface, null, width);
    }

    public AttachmentPointUI(Point coord, CardinalPoint cardinal, SurfaceUI parentSurface, Cursor cursor, double width) {
        pointWidth = width;
        rectangle = new Rectangle(coord.x - (width / 2), coord.y - (width / 2), width, width);

        if (cursor != null) {
            rectangle.setCursor(cursor);
        } else if (cardinal == CardinalPoint.SE) {
            rectangle.setCursor(Cursor.SE_RESIZE);
        }

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
