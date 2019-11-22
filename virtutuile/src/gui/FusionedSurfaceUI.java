package gui;

import application.SealsInfoDto;
import application.SurfaceDto;
import application.TileDto;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Shape;
import utils.Id;
import utils.Point;

import java.util.List;

public class FusionedSurfaceUI implements SurfaceUI {

    private Shape shape;
    private List<SurfaceUI> initalSurfaces;

    public FusionedSurfaceUI(List<SurfaceUI> allSurfacesToFusionne, Pane parentNode) {

        Shape firstShape = allSurfacesToFusionne.get(0).getMainShape();
        this.shape = firstShape;

        allSurfacesToFusionne.forEach(s -> {
            s.hide();

            if (s.getMainShape() == this.shape) {
                return;
            }
            this.shape = Shape.union(this.shape, s.getMainShape());
        });
        this.initalSurfaces = allSurfacesToFusionne;

        this.shape.setFill(firstShape.getFill());
        this.shape.setStroke(firstShape.getStroke());

        parentNode.getChildren().addAll(shape);
    }

    @Override
    public Node getNode() {
        return this.shape;
    }

    @Override
    public Shape getMainShape() {
        return this.shape;
    }

    @Override
    public SurfaceDto toDto() {
        return null;
    }

    @Override
    public void select() {

    }

    @Override
    public void unselect() {

    }

    @Override
    public Id getId() {
        return null;
    }

    @Override
    public void delete() {

    }

    @Override
    public void setMasterTile(TileDto tileHeight) {

    }

    @Override
    public TileDto getMasterTile() {
        return null;
    }

    @Override
    public void setSealsInfo(SealsInfoDto newSealInfos) {

    }

    @Override
    public SealsInfoDto getSealsInfo() {
        return null;
    }

    @Override
    public void hideTiles() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void fill() {

    }

    @Override
    public void setSize(double width, double height) {

    }

    @Override
    public void setPosition(Point position) {

    }

    @Override
    public void setHole(boolean isHole) {

    }
}
