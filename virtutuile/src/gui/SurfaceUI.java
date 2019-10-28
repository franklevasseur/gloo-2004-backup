package gui;

import application.SurfaceDto;
import javafx.scene.Node;
import utils.Id;

public interface SurfaceUI {

    public Node getNode();
    public SurfaceDto toDto();
    public void select();
    public void unselect();
    public Id getId();
    public void remove();
}
