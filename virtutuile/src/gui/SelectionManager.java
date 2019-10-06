package gui;

import java.util.LinkedList;
import java.util.List;

public class SelectionManager {

    private List<RectangleSurfaceUI> selectedSurfaces = new LinkedList<RectangleSurfaceUI>();

    public void unselectAll() {
        selectedSurfaces.forEach(RectangleSurfaceUI::unselect);
        selectedSurfaces.clear();
    }

    public void selectSurface(RectangleSurfaceUI surface) {
        selectedSurfaces.add(surface);
    }

    public List<RectangleSurfaceUI> getSelectedSurfaces() {
        return selectedSurfaces;
    }
}
