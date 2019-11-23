package application;

import Domain.Project;
import Domain.Surface;
import utils.Point;
import utils.RectangleHelper;
import utils.RectangleInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Controller {

    private static Controller instance = new Controller();
    public static Controller getInstance() {
        return instance;
    }
    private UndoRedoManager undoRedoManager = new UndoRedoManager();

    private Project vraiProject;

    private Controller() {
        this.vraiProject = new Project();
        undoRedoManager.justDoIt(ProjectAssembler.toDto(vraiProject));
    }

    public void createSurface(SurfaceDto newSurfaceDto) {
        Surface newSurface = SurfaceAssembler.fromDto(newSurfaceDto);
        vraiProject.getSurfaces().add(newSurface);
        undoRedoManager.justDoIt(ProjectAssembler.toDto(vraiProject));
    }

    public void updateSurface(SurfaceDto surfaceDto) {
        Surface surface = this.vraiProject.getSurfaces().stream().filter(s -> s.getId().isSame(surfaceDto.id)).findFirst().get();
        SurfaceAssembler.fromDto(surfaceDto, surface);
        undoRedoManager.justDoIt(ProjectAssembler.toDto(vraiProject));
    }

    public ProjectDto getProject() {
        return ProjectAssembler.toDto(vraiProject);
        // ...
    }

    public void removeSurface(SurfaceDto surface) {
        this.vraiProject.getSurfaces().removeIf(s -> s.getId().isSame(surface.id));
        undoRedoManager.justDoIt(ProjectAssembler.toDto(vraiProject));
    }

    public void undo() {
        ProjectDto dto = undoRedoManager.undo();
        if (dto == null) {
            return;
        }
        this.vraiProject = ProjectAssembler.fromDto(dto);
    }

    public void redo() {
        ProjectDto dto = undoRedoManager.redo();
        if (dto == null) {
            return;
        }
        this.vraiProject = ProjectAssembler.fromDto(dto);
    }

    public boolean redoAvailable() {
        return undoRedoManager.redoAvailable();
    }

    public boolean undoAvailable() {
        return undoRedoManager.undoAvailable();
    }

    public List<TileDto> fillSurface(SurfaceDto surface, TileDto masterTile, PatternDto patternDto, SealsInfoDto sealing) {
        List<TileDto> tiles = this.fillSurfaceWithDefaults(surface);
        undoRedoManager.justDoIt(ProjectAssembler.toDto(vraiProject));

        return tiles;
    }

    private List<TileDto> fillSurfaceWithDefaults(SurfaceDto surfaceToFill) {
        // Let the backend choose a default pattern and sealing and master tile
        // ...
        Surface desiredSurface = this.vraiProject.getSurfaces().stream().filter(s -> s.getId().isSame(surfaceToFill.id)).findFirst().get();
        SurfaceDto surfaceToFillDto = SurfaceAssembler.toDto(desiredSurface);

        if (!surfaceToFillDto.isRectangular) {
            // TODO: find another logic
            throw new RuntimeException("Ça va te prendre une logique par defaut pour remplir des surfaces irrégulières mon ti-chum");
        }

        RectangleInfo surfaceRectangle = RectangleHelper.summitsToRectangleInfo(surfaceToFillDto.summits);

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

        surfaceToFillDto.tiles = tiles;

        //TODO: je sais c'est pas bin beau
        this.updateSurface(surfaceToFillDto);

        return tiles;
    }

    public void loadProject(String projectPath) {
        // ...
    }

    public void saveProject(String projectPath) {
        // ...
    }

    public void fusionSurfaces(List<SurfaceDto> surfacesDto) {
        List<Surface> surfaces = surfacesDto.stream().map(dto -> SurfaceAssembler.fromDto(dto)).collect(Collectors.toList());
        this.vraiProject.fusionSurfaces(surfaces);
        undoRedoManager.justDoIt(ProjectAssembler.toDto(vraiProject));
    }

    // ...
}
