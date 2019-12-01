package Domain;

import utils.Color;
import utils.Id;

import java.io.Serializable;

public class Material implements Serializable {
    // TODO : je suis pas sûr mais ca serait quand meme logique d'avoir la taille de la tuile type dans le materiaux
    private utils.Color color;
    private MaterialType materialType;
    private String materialName;
    private int nbTilePerBox;
    private double costPerBox;
    private double tileTypeWidth = 0.2;
    private double tileTypeHeight = 0.4;

    public Material(Color pColor, MaterialType pType, String pName, int pNbTiles, double pCostBox, double pTileWidth, double pTileHeight){
        this.color = pColor;
        this.materialType = pType;
        this.materialName = pName;
        this.nbTilePerBox = pNbTiles;
        this.costPerBox = pCostBox;
        this.tileTypeHeight = pTileHeight;
        this.tileTypeWidth = pTileWidth;
    }
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

    public int getNbTilePerBox() {
        return nbTilePerBox;
    }

    public double getCostPerBox() {
        return costPerBox;
    }

    public double getTileTypeHeight() {
        return tileTypeHeight;
    }

    public double getTileTypeWidth() {
        return tileTypeWidth;
    }

    public void setCostPerBox(double costPerBox) {
        this.costPerBox = costPerBox;
    }

    public void setNbTilePerBox(int nbTilePerBox) {
        this.nbTilePerBox = nbTilePerBox;
    }

    public void setTileTypeHeight(double tileTypeHeight) {
        this.tileTypeHeight = tileTypeHeight;
    }

    public void setTileTypeWidth(double tileTypeWidth) {
        this.tileTypeWidth = tileTypeWidth;
    }
}
