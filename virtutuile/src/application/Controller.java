package application;

import java.util.ArrayList;

public class Controller {

    private static Controller instance = new Controller();
    public static Controller getInstance() {
        return instance;
    }

    // TODO: remove this and replace with a backend;
    private ProjectDto temporaryProject = new ProjectDto();

    public void createSurface(SurfaceDto newSurface) {
        if (temporaryProject.surfaces == null) {
            temporaryProject.surfaces = new ArrayList<SurfaceDto>();
        }
        temporaryProject.surfaces.add(newSurface);

        // ...
    }

    public void updateSurface(SurfaceDto surface) {
        this.removeSurface(surface);
        temporaryProject.surfaces.add(surface);
        // ...
    }

    public ProjectDto getProject() {
        return temporaryProject;
        // ...
    }

    public void removeSurface(SurfaceDto surface) {
        this.temporaryProject.surfaces.removeIf(s -> s.id.isSame(surface.id));
        // ...
    }

    public void undo() {
        // ...
    }

    public void redo() {
        // ...
    }

    public void fillSurface(SurfaceDto surface, TileDto masterTile, PatternDto patternDto, SealsinfoDto sealing) {
        // ...
    }

    public void loadProject(String projectPath) {
        // ...
    }

    public void saveProject(String projectPath) {
        // ...
    }

    // ...
}
