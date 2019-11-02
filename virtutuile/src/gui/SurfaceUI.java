package gui;

import application.SurfaceDto;
import javafx.scene.Node;
import utils.Id;

public interface SurfaceUI {

    Node getNode();
    SurfaceDto toDto();
    void select();
    void unselect();
    Id getId();
    void delete();
    void hide();
}
