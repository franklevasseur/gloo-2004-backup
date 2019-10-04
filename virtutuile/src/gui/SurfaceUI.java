package gui;

import javafx.event.EventHandler;
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
                isSelected = true;
                rectangle.setFill(Color.RED);
                selectionManager.unselectAll();
                selectionManager.selectSurface(that);
                t.consume();
            }
        });
    }

    public Node getNode() {
        return rectangle;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void unselect() {
        isSelected = false;
        rectangle.setFill(Color.BLACK);
    }
}
