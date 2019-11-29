package Domain;

import java.util.ArrayList;
import java.util.List;

public class Project {
    private List<Surface> surfaces = new ArrayList<>();
    private List<Material> materials = new ArrayList<>();

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
            if (s.isFusionned()) {
                baseSurfaces.addAll(((FusionnedSurface) s).getFusionnedSurfaces());
            } else {
                baseSurfaces.add(s);
            }
        }

        FusionnedSurface newFusionnedSurface = new FusionnedSurface(baseSurfaces);
        this.surfaces.add(newFusionnedSurface);
    }

    public void unfusionSurfaces(FusionnedSurface fusionnedSurface) {
        this.surfaces.removeIf(s -> s == fusionnedSurface);
        this.surfaces.addAll(fusionnedSurface.getFusionnedSurfaces());
    }
}
