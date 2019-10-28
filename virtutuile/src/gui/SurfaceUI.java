package gui;

import application.SurfaceDto;
import javafx.scene.Node;

public interface SurfaceUI {

    public Node getNode();
    public SurfaceDto toDto();
    public void select();
    public void unselect();
}
