package gui;

import application.SealsInfoDto;
import application.SurfaceDto;
import application.TileDto;
import javafx.scene.Node;
import utils.Id;
import utils.Point;

public interface SurfaceUI {

    Node getNode();
    SurfaceDto toDto();
    void select();
    void unselect();
    Id getId();
    void delete();
    void setMasterTile(TileDto tileHeight);
    TileDto getMasterTile();
    void setSealsInfo(SealsInfoDto newSealInfos);
    SealsInfoDto getSealsInfo();
    void hideTiles();
    void hide();
    void fill();
    void setSize(double width, double height);
    void setPosition(Point position);
    void setHole(boolean isHole);
}
