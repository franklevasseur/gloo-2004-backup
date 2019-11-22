package Domain;

import java.util.List;

public class FusionnedSurface {

    private List<Surface> fusionnedSurfaces;

    public FusionnedSurface(List<Surface> fusionnedSurfaces) {
        this.fusionnedSurfaces = fusionnedSurfaces;
    }

    public List<Surface> getFusionnedSurfaces() {
        return this.fusionnedSurfaces;
    }
}
