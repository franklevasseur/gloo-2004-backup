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
        // TODO: à vincent
        // ...
    }
}
