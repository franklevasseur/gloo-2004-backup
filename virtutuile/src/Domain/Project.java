package Domain;

import java.util.ArrayList;
import java.util.List;

public class Project {
    private List<Surface> surfaces = new ArrayList<>();
    private List<FusionnedSurface> fusionnedSurfaces = new ArrayList<>();
    private List<Material> materials = new ArrayList<>();

    public List<Material> getMaterials() {
        return materials;
    }

    public List<Surface> getSurfaces() {
        return surfaces;
    }

    public List<FusionnedSurface> getFusionnedSurfaces() {
        return this.fusionnedSurfaces;
    }

    public void setMaterials(List<Material> materials) {
        this.materials = materials;
    }

    public void setSurfaces(List<Surface> surfaces) {
        this.surfaces = surfaces;
    }

    public void fusionSurfaces(List<Surface> surfaces) {
        this.surfaces.removeIf(s -> surfaces.contains(s));
        FusionnedSurface newFusionnedSurface = new FusionnedSurface(surfaces);
        this.fusionnedSurfaces.add(newFusionnedSurface);
    }

    public void setFusionnedSurfaces(List<FusionnedSurface> fusionnedSurfaces) {
        this.fusionnedSurfaces = fusionnedSurfaces;
    }
}
