package Domain;


public class SealsInfo {
    private Measure width;
    private Material material;

    public SealsInfo(Measure pWidth, Material pMaterial){
        width = pWidth;
        material = pMaterial;
    }

    public Material getMaterial() {
        return material;
    }

    public Measure getWidth() {
        return width;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public void setWidth(Measure width) {
        this.width = width;
    }
}
