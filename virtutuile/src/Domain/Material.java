package Domain;

import utils.Color;

import java.io.Serializable;

public class Material implements Serializable {
    private utils.Color color;
    private String materialName;

    private int nbTilePerBox;
    private double costPerBox;
    private double tileTypeWidth = 0.2;
    private double tileTypeHeight = 0.4;

    public Material(Color pColor, String pName, int pNbTiles, double pCostBox, double pTileWidth, double pTileHeight) {
        this.color = pColor;
        this.materialName = pName;
        this.nbTilePerBox = pNbTiles;
        this.costPerBox = pCostBox;
        this.tileTypeHeight = pTileHeight;
        this.tileTypeWidth = pTileWidth;
    }

    public Material(Color pColor, String pName) {
        this.color = pColor;
        this.materialName = pName;
    }

    public String getMaterialName() {
        return materialName;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
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
