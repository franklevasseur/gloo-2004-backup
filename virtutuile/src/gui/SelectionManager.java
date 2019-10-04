package gui;

import java.util.LinkedList;
import java.util.List;

public class SelectionManager {

    private List<SurfaceUI> selectedSurfaces = new LinkedList<SurfaceUI>();

    public void unselectAll() {
        selectedSurfaces.forEach(SurfaceUI::unselect);
        selectedSurfaces.clear();
    }

    public void selectSurface(SurfaceUI surface) {
        selectedSurfaces.add(surface);
    }

    public List<SurfaceUI> getSelectedSurfaces() {
        return selectedSurfaces;
    }
}
