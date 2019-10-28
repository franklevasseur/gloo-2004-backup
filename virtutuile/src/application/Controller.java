package application;

import utils.Point;
import utils.RectangleHelper;
import utils.RectangleInfo;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    public void fillSurface(SurfaceDto surface, TileDto masterTile, PatternDto patternDto, SealsInfoDto sealing) {
        // ...
    }

    public void fillSurfaceWithDefaults(SurfaceDto surfaceToFill) {
        // Let the backend choose a default pattern and sealing and master tile
        // ...
        SurfaceDto surface = this.temporaryProject.surfaces.stream().filter(s -> s.id.isSame(surfaceToFill.id)).findFirst().get();
        surface.isHole = false;

        if (!surface.isRectangular) {
            // TODO: find another logic
            throw new RuntimeException("Ça va te prendre une logique par defaut pour remplir des surfaces irrégulières mon ti-chum");
        }

        RectangleInfo surfaceRectangle = RectangleHelper.summitsToRectangleInfo(surface.summits);

        double tileWidth = 0.3;
        double tileHeight = 0.2;

        SealsInfoDto defaultSealInfo = new SealsInfoDto();
        defaultSealInfo.sealWidth = 0.02;

        List<TileDto> tiles = new ArrayList<>();

        double unitOfWidth = tileWidth + defaultSealInfo.sealWidth;
        double unitOfHeight = tileHeight + defaultSealInfo.sealWidth;

        int amountOfLines = (int) Math.ceil(surfaceRectangle.height / unitOfHeight);
        int amountOfColumns = (int) Math.ceil(surfaceRectangle.width / unitOfWidth);

        for (int line = 0; line < amountOfLines; line++) {
            for (int column = 0; column < amountOfColumns; column++) {
                TileDto nextTile = new TileDto();

                Point topLeftCorner = Point.translate(surfaceRectangle.topLeftCorner, column * unitOfWidth, line * unitOfHeight);

                double rightSurfaceBound = surfaceRectangle.topLeftCorner.x + surfaceRectangle.width;
                double bottomSurfaceBound = surfaceRectangle.topLeftCorner.y + surfaceRectangle.height;

                boolean isTileOverflowX = topLeftCorner.x + tileWidth > rightSurfaceBound;
                boolean isTileOverflowY = topLeftCorner.y + tileHeight > bottomSurfaceBound;
                double actualWidth = isTileOverflowX ? rightSurfaceBound - topLeftCorner.x : tileWidth;
                double actualHeight = isTileOverflowY ? bottomSurfaceBound - topLeftCorner.y : tileHeight;

                nextTile.summits = RectangleHelper.rectangleInfoToSummits(topLeftCorner, actualWidth, actualHeight);

                tiles.add(nextTile);
            }
        }

        surface.tiles = tiles;
    }

    public void loadProject(String projectPath) {
        // ...
    }

    public void saveProject(String projectPath) {
        // ...
    }

    // ...
}
