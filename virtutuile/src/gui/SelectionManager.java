package gui;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class SelectionManager {

    private List<SurfaceUI> selectedSurfaces = new ArrayList<>();
    private boolean multipleSelectionAllowed = false;

    public void unselectAll() {
        selectedSurfaces.forEach(SurfaceUI::unselect);
        selectedSurfaces.clear();
    }

    public void selectSurface(RectangleSurfaceUI surface) {
        if (!multipleSelectionAllowed) {
            unselectAll();
        }
        surface.select();
        selectedSurfaces.add(surface);
    }

    public List<SurfaceUI> getSelectedSurfaces() {
        return selectedSurfaces;
    }

    public void allowMultipleSelection() {
        multipleSelectionAllowed = true;
    }

    public void disableMultipleSelection() {
        multipleSelectionAllowed = false;
    }
}
