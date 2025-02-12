package gui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class SelectionManager {

    private List<SurfaceUI> selectedSurfaces = new ArrayList<>();
    private boolean multipleSelectionAllowed = false;
    private Function<Void, Void> handler;

    public SelectionManager(Function<Void, Void> handler) {
        this.handler = handler;
    }

    public void unselectAll() {
        selectedSurfaces.forEach(SurfaceUI::unselect);
        selectedSurfaces.clear();
    }

    public void selectSurface(SurfaceUI surface) {
        if (!multipleSelectionAllowed) {
            unselectAll();
            surface.select(true);
        } else {
            surface.select(false);
        }

        if (!selectedSurfaces.contains(surface)) {
            selectedSurfaces.add(surface);
        }

        handler.apply(null);
    }

    public void unselectSurface(SurfaceUI surface) {
        surface.unselect();
        selectedSurfaces.removeIf(s -> s.getId() == surface.getId());
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
