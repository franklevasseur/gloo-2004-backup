package application;

import Domain.*;
import utils.*;
import utils.Point;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Controller {

    private static Controller instance = new Controller();
    public static Controller getInstance() {
        return instance;
    }
    private UndoRedoManager undoRedoManager = new UndoRedoManager();

    private InspectionService inspector = InspectionService.getInstance();

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
        this.internalUpdateSurface(surfaceDto);
        undoRedoManager.justDoIt(ProjectAssembler.toDto(vraiProject));
    }

    private void internalUpdateSurface(SurfaceDto surfaceDto) {
        Surface surface = this.vraiProject.getSurfaces().stream().filter(s -> s.getId().isSame(surfaceDto.id)).findFirst().get();
        SurfaceAssembler.fromDto(surfaceDto, surface);
    }

    // atomic move and fill or resize and fill
    public List<TileDto> updateAndRefill(SurfaceDto dto, application.TileDto masterTile, PatternDto patternDto, SealsInfoDto sealing)  {
        internalUpdateSurface(dto);
        List<TileDto> tiles = fillSurface(dto, masterTile, patternDto, sealing);
        internalUpdateSurface(dto);
        undoRedoManager.justDoIt(ProjectAssembler.toDto(vraiProject));
        return tiles;
    }

    public ProjectDto getProject() {
        return ProjectAssembler.toDto(vraiProject);
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

    public List<TileDto> fillSurface(SurfaceDto dto, TileDto masterTileDto, PatternDto patternDto, SealsInfoDto sealingDto) {
        TileDto actualUsedMasterTile;
        if (masterTileDto == null) {
            actualUsedMasterTile = new TileDto();
            actualUsedMasterTile.summits = RectangleHelper.rectangleInfoToSummits(ShapeHelper.getTopLeftCorner(new AbstractShape(dto.summits, false)), 0.2, 0.3);
            actualUsedMasterTile.material = MaterialAssembler.toDto(vraiProject.getMaterials().get(0));
        } else {
            actualUsedMasterTile = masterTileDto;
        }

        SealsInfoDto actualSealInfo;
        if (sealingDto == null) {
            actualSealInfo = new SealsInfoDto();
            actualSealInfo.color = Color.BLUE;
            actualSealInfo.sealWidth = 0.02;
        } else {
            actualSealInfo = sealingDto;
        }

        List<TileDto> tiles = this.fillSurfaceWithDefaults(dto, actualUsedMasterTile, patternDto, actualSealInfo);
        this.internalUpdateSurface(dto);
        undoRedoManager.justDoIt(ProjectAssembler.toDto(vraiProject));

        return tiles;

        // TODO: Arnaud essaye donc de décommenter ce qui est en bas pis de faire marcher ca plz
//        Surface desiredSurface = this.vraiProject.getSurfaces().stream().filter(s -> s.getId().isSame(dto.id)).findFirst().get();
//
//        desiredSurface.fillSurface(SurfaceAssembler.fromDto(actualUsedMasterTile), SurfaceAssembler.fromDto(actualSealInfo), PatternType.TYPE1);
//
//        SurfaceDto tiles = SurfaceAssembler.toDto(desiredSurface);
//
//        this.internalUpdateSurface(dto);
//        undoRedoManager.justDoIt(ProjectAssembler.toDto(vraiProject));
//
//        return tiles.tiles;
    }

    private List<TileDto> fillSurfaceWithDefaults(SurfaceDto surfaceToFillDto, TileDto masterTile, PatternDto patternDto, SealsInfoDto sealing) {

        AbstractShape surfaceShape = new AbstractShape(surfaceToFillDto.summits, false);
        Point surfaceTopLeftCorner = ShapeHelper.getTopLeftCorner(surfaceShape);
        double surfaceWidth = ShapeHelper.getWidth(surfaceShape);
        double surfaceHeight = ShapeHelper.getHeight(surfaceShape);

        RectangleInfo info = RectangleHelper.summitsToRectangleInfo(masterTile.summits);
        double tileWidth = info.width;
        double tileHeight = info.height;

        List<TileDto> tiles = new ArrayList<>();

        double unitOfWidth = tileWidth + sealing.sealWidth;
        double unitOfHeight = tileHeight + sealing.sealWidth;

        int amountOfLines = (int) Math.ceil(surfaceHeight / unitOfHeight);
        int amountOfColumns = (int) Math.ceil(surfaceWidth / unitOfWidth);

        for (int line = 0; line < amountOfLines; line++) {
            for (int column = 0; column < amountOfColumns; column++) {
                TileDto nextTile = new TileDto();


                Point topLeftCorner = Point.translate(surfaceTopLeftCorner, column * unitOfWidth, line * unitOfHeight);

                double rightSurfaceBound = surfaceTopLeftCorner.x + surfaceWidth;
                double bottomSurfaceBound = surfaceTopLeftCorner.y + surfaceHeight;

                boolean isTileOverflowX = topLeftCorner.x + tileWidth > rightSurfaceBound;
                boolean isTileOverflowY = topLeftCorner.y + tileHeight > bottomSurfaceBound;
                double actualWidth = isTileOverflowX ? rightSurfaceBound - topLeftCorner.x : tileWidth;
                double actualHeight = isTileOverflowY ? bottomSurfaceBound - topLeftCorner.y : tileHeight;

                nextTile.summits = RectangleHelper.rectangleInfoToSummits(topLeftCorner, actualWidth, actualHeight);
                nextTile.material = masterTile.material;

                tiles.add(nextTile);
            }
        }

        surfaceToFillDto.tiles = tiles;
        surfaceToFillDto.isHole = HoleStatus.FILLED;
        surfaceToFillDto.sealsInfoDto = sealing;

        return tiles;
    }

    public void loadProject(String projectPath) {
        // ...
    }

    public void saveProject(String projectPath) {
        // ...
    }

    public void fusionSurfaces(List<SurfaceDto> surfacesDto) {
        List<Surface> surfaces = surfacesDto.stream().map(dto -> {
            return this.vraiProject.getSurfaces().stream().filter(s -> s.getId().isSame(dto.id)).findFirst().get();
        }).collect(Collectors.toList());
        this.vraiProject.fusionSurfaces(surfaces);
        undoRedoManager.justDoIt(ProjectAssembler.toDto(vraiProject));
    }

    public void createMaterial(MaterialDto dto) {
        Material material = MaterialAssembler.fromDto(dto);
        vraiProject.getMaterials().add(material);
        // TODO: avertir undo/redo que ca vient de se passer (Philippe ne pas enlever ce todo, c'est pour Frank)
    }

    public String inspect() {
        return inspector.inspect(this.vraiProject);
    }
}
