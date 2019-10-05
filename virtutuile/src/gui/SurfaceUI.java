package gui;

import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class SurfaceUI {

    private Rectangle rectangle;
    private boolean isSelected = false;

    public SurfaceUI(double x, double y, double width, double height, SelectionManager selectionManager) {
        rectangle = new Rectangle(x, y, width, height);

        SurfaceUI that = this;
        rectangle.setOnMouseClicked(new EventHandler<MouseEvent>()
        {
            @Override
            public void handle(MouseEvent t) {
                selectionManager.unselectAll();

                select();
                selectionManager.selectSurface(that);
                t.consume();
            }
        });

        rectangle.setOnMouseDragged(new EventHandler<MouseEvent>()
        {
            @Override
            public void handle(MouseEvent t) {
                if (isSelected) {
                    rectangle.setX(t.getX() - (width / 2));
                    rectangle.setY(t.getY() - (height / 2));
                    t.consume();
                }
            }
        });
    }

    public Node getNode() {
        return rectangle;
    }

    void select() {
        isSelected = true;
        rectangle.setFill(Color.RED);
        rectangle.setCursor(Cursor.HAND);
    }

    void unselect() {
        isSelected = false;
        rectangle.setFill(Color.BLACK);
        rectangle.setCursor(Cursor.DEFAULT);
    }
}
