package Domain;

import utils.Color;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Project implements Serializable {
    private static final long serialVersionUID = 420L;
    private List<Surface> surfaces = new ArrayList<>();
    private List<Material> materials = new ArrayList<>();

    public Project() {
        createDefaultMaterial();
    }

    public List<Material> getMaterials() {
        return materials;
    }

    public List<Surface> getSurfaces() {
        return surfaces;
    }

    public void setMaterials(List<Material> materials) {
        this.materials = materials;
    }

    public void setSurfaces(List<Surface> surfaces) {
        this.surfaces = surfaces;
    }

    public void fusionSurfaces(List<Surface> surfaces) {
        this.surfaces.removeIf(s -> surfaces.contains(s));

        List<Surface> baseSurfaces = new ArrayList<>();
        for (Surface s : surfaces) {

            if (s.isHole() == HoleStatus.FILLED) {
                s.setHole(HoleStatus.NONE);
            }

            if (s.isFusionned()) {
                baseSurfaces.addAll(((FusionnedSurface) s).getFusionnedSurfaces());
            } else {
                baseSurfaces.add(s);
            }
        }

        FusionnedSurface newFusionnedSurface = new FusionnedSurface(baseSurfaces);
        this.surfaces.add(newFusionnedSurface);
    }

    public void removeSurface(Surface surface) {
        this.surfaces.remove(surface);
    }

    public void removeMaterial(Material material) {
        this.materials.remove(material);
    }

    public void unfusionSurfaces(FusionnedSurface fusionnedSurface) {
        this.surfaces.removeIf(s -> s == fusionnedSurface);
        this.surfaces.addAll(fusionnedSurface.getFusionnedSurfaces());
    }

    private void createDefaultMaterial() {
        Material waterMelon = new Material(Color.GREEN, "Melon d'eau", 45, 50, 0.6, 0.3);
        Material banana = new Material(Color.YELLOW, "Banane", 35, 90, 0.4, 0.9);
        this.materials.add(waterMelon);
        this.materials.add(banana);
    }
}
