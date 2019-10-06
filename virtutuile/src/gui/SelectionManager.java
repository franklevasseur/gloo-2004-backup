package gui;

import java.util.LinkedList;
import java.util.List;

public class SelectionManager {

    private List<RectangleSurfaceUI> selectedSurfaces = new LinkedList<RectangleSurfaceUI>();
    private boolean multipleSelectionAllowed = false;

    public void unselectAll() {
        selectedSurfaces.forEach(RectangleSurfaceUI::unselect);
        selectedSurfaces.clear();
    }

    public void selectSurface(RectangleSurfaceUI surface) {
        if (!multipleSelectionAllowed) {
            unselectAll();
        }
        surface.select();
        selectedSurfaces.add(surface);
    }

    public List<RectangleSurfaceUI> getSelectedSurfaces() {
        return selectedSurfaces;
    }

    public void allowMultipleSelection() {
        multipleSelectionAllowed = true;
    }

    public void disableMultipleSelection() {
        multipleSelectionAllowed = false;
    }
}
