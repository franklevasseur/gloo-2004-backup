package gui;

import application.SealsInfoDto;
import application.SurfaceDto;
import application.TileDto;
import javafx.scene.Node;
import utils.Id;

public interface SurfaceUI {

    public Node getNode();
    public SurfaceDto toDto();
    public void select();
    public void unselect();
    public Id getId();
    public void remove();
    public void setMasterTile(TileDto tileHeight);
    public TileDto getMasterTile();
    public void setSealsInfo(SealsInfoDto newSealInfos);
    public SealsInfoDto getSealsInfo();
    public void hideTiles();
}
