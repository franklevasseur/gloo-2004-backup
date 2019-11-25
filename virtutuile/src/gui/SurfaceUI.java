package gui;

import Domain.HoleStatus;
import application.SealsInfoDto;
import application.SurfaceDto;
import application.TileDto;
import javafx.scene.Node;
import javafx.scene.shape.Shape;
import utils.Id;
import utils.Point;

public interface SurfaceUI {

    Node getNode();
    Shape getMainShape(); // gives the rectangle without the tiles and anchor points
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
    void forceFill();
    void setSize(double width, double height);
    void setPosition(Point position);
    void translatePixelBy(Point translation);
    void setHole(HoleStatus isHole);
}
