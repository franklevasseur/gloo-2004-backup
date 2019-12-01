package Domain;

import utils.Color;
import utils.Id;

import java.io.Serializable;

public class Material implements Serializable {
    private utils.Color color;
    private MaterialType materialType;
    private String materialName;
    public Material(Color pColor, MaterialType pType, String pName){
        this.color = pColor;
        this.materialType = pType;
        this.materialName = pName;


    }

    public void setMaterialName(String materialName) {
        this.materialName = materialName;
    }

    public String getMaterialName() {
        return materialName;
    }

    public Color getColor() {
        return color;
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
