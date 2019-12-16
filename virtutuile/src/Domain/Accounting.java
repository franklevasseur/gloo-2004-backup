package Domain;

public class Accounting {

    private Material material;
    private int totalTiles;

    public Accounting(Material pMaterials, int totalTiles) {
        this.material = pMaterials;
        this.totalTiles = totalTiles;
    }

    public Material getMaterial() {
        return this.material;
    }

    public int getNbBoxes() {
        double exactNumberOfBoxes = ((double) totalTiles) / ((double) material.getNbTilePerBox());
        return (int) Math.ceil(exactNumberOfBoxes);
    }

    public double getTotalCost() {
        return this.getNbBoxes() * material.getCostPerBox();
    }

    public int getUsedTiles() {
        return this.totalTiles;
    }

    public int getAllTiles() {
        return material.getNbTilePerBox() * getNbBoxes();
    }

    public void incrementNbTiles(int deltaTiles) {
        this.totalTiles += deltaTiles;
    }
}
