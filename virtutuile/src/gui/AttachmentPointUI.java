package gui;

import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class AttachmentPointUI {

    private static final int pointWidth = 10;
    private Rectangle rectangle;

    public AttachmentPointUI(PixelPoint coord, CardinalPoint cardinal, RectangleSurfaceUI parentSurface) {
        rectangle = new Rectangle(coord.x - (pointWidth / 2), coord.y - (pointWidth / 2), pointWidth, pointWidth);

        rectangle.setOnMouseEntered(new EventHandler<MouseEvent>()
        {
            @Override
            public void handle(MouseEvent t) {
//                if (cardinal == CardinalPoint.NW) {
//                    rectangle.setCursor(Cursor.NW_RESIZE);
//                } else if (cardinal == CardinalPoint.NE) {
//                    rectangle.setCursor(Cursor.NE_RESIZE);
//                } else if (cardinal == CardinalPoint.SE) {
//                    rectangle.setCursor(Cursor.SE_RESIZE);
//                } else if (cardinal == CardinalPoint.SW) {
//                    rectangle.setCursor(Cursor.SW_RESIZE);
//                } else if (cardinal == CardinalPoint.W) {
//                    rectangle.setCursor(Cursor.W_RESIZE);
//                } else if (cardinal == CardinalPoint.N) {
//                    rectangle.setCursor(Cursor.N_RESIZE);
//                } else if (cardinal == CardinalPoint.E) {
//                    rectangle.setCursor(Cursor.E_RESIZE);
//                } else if (cardinal == CardinalPoint.S) {
//                    rectangle.setCursor(Cursor.S_RESIZE);
//                }
                if (cardinal == CardinalPoint.SE) {
                    rectangle.setCursor(Cursor.SE_RESIZE);
                }
            }
        });

        rectangle.setOnMouseExited(new EventHandler<MouseEvent>()
        {
            @Override
            public void handle(MouseEvent t) {
                rectangle.setCursor(Cursor.DEFAULT);
            }
        });

        rectangle.setOnMouseDragged(new EventHandler<MouseEvent>()
        {
            @Override
            public void handle(MouseEvent t) {
                if (cardinal != CardinalPoint.SE) {
                    return;
                }

                double deltaX = t.getX() - (rectangle.getX() + pointWidth / 2);
                double deltaY = t.getY() - (rectangle.getY() + pointWidth / 2);

                rectangle.setX(t.getX() - (pointWidth / 2));
                rectangle.setY(t.getY() - (pointWidth / 2));

                parentSurface.increaseSizeBy(deltaX, deltaY);
                t.consume();
            }
        });

        rectangle.setOnMouseClicked(new EventHandler<MouseEvent>()
        {
            @Override
            public void handle(MouseEvent t) {
                parentSurface.unselect();
                parentSurface.select();
                t.consume();
            }
        });

        rectangle.setFill(Color.GRAY);
    }

    public Node getNode() {
        return rectangle;
    }
}
