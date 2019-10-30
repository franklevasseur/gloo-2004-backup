package Domain;

import utils.Id;

public class Material {
    private Color color;
    private MaterialType materialType;
    private Id id;

    public Material(Color pColor, MaterialType pType){
        this.color = pColor;
        this.materialType = pType;
        id = new Id();
    }

    public Color getColor() {
        return color;
    }

    public Id getId() {
        return id;
    }

    public MaterialType getMaterialType() {
        return materialType;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public void setMaterialType(MaterialType materialType) {
        this.materialType = materialType;
    }
}
