package application;

import Domain.*;
import utils.*;
import utils.Point;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Controller {

    private static Controller instance = new Controller();

    public static Controller getInstance() {
        return instance;
    }

    private UndoRedoManager undoRedoManager = new UndoRedoManager();

    public List<Accounting> Maccount = new ArrayList<>();

    private InspectionService inspector = new InspectionService();
    private ProjectRepository projectRepository;
    private MaterialService materialService;
    private SurfaceService surfaceService;
    private SurfaceAssembler surfaceAssembler;
    private MaterialAssembler materialAssembler = new MaterialAssembler();
    private ProjectAssembler projectAssembler;
    private TileAssembler tileAssembler;
    private SealingAssembler sealingAssembler = new SealingAssembler();

    private Function<Void, Void> undoRedoListener;

    private Controller() {
        Project vraiProject = new Project();
        this.projectRepository = new ProjectRepository(vraiProject);
        this.materialService = new MaterialService(projectRepository);
        this.surfaceService = new SurfaceService(projectRepository);

        this.tileAssembler = new TileAssembler(materialAssembler, materialService);

        this.surfaceAssembler = new SurfaceAssembler(tileAssembler, sealingAssembler);
        this.projectAssembler = new ProjectAssembler(surfaceAssembler, materialAssembler);

        justDoIt();
    }

    public void removeSurface(SurfaceDto surface) {
        this.projectRepository.getProject().getSurfaces().removeIf(s -> s.getId().isSame(surface.id));

        justDoIt();
    }

    public void createSurface(SurfaceDto newSurfaceDto) {
        Surface newSurface = surfaceAssembler.fromDto(newSurfaceDto);
        projectRepository.getProject().getSurfaces().add(newSurface);

        justDoIt();
    }

    public void createMaterial(MaterialDto dto) {
        Material material = materialAssembler.fromDto(dto);
        projectRepository.getProject().getMaterials().add(material);

        justDoIt();
    }

    public void updateSurface(SurfaceDto surfaceDto) {
        this.internalUpdateSurface(surfaceDto);
        justDoIt();
    }

    private void internalUpdateSurface(SurfaceDto surfaceDto) {
        Surface surface = surfaceService.getSurfaceById(surfaceDto.id).get();
        surfaceAssembler.fromDto(surfaceDto, surface); // does update all surface fields
    }

    public void updateMaterial(MaterialDto materialDto) {
        Material material = materialService.getMaterialByName(materialDto.name).get();
        materialAssembler.fromDto(materialDto, material);
        surfaceService.updateMaterial(material);

        justDoIt();
    }

    // atomic move and fill or resize and fill
    public List<TileDto> updateAndRefill(SurfaceDto dto, application.TileDto masterTile, PatternType patternDto, SealsInfoDto sealing, double angle, double shift) {
        internalUpdateSurface(dto);
        return fillSurface(dto, masterTile, patternDto, sealing, angle, shift);
    }

    public ProjectDto getProject() {
        return projectAssembler.toDto(projectRepository.getProject());
    }

    public void undo() {
        ProjectDto dto = undoRedoManager.undo();
        if (dto == null) {
            return;
        }
        this.projectRepository.setProject(projectAssembler.fromDto(dto));
    }

    public void redo() {
        ProjectDto dto = undoRedoManager.redo();
        if (dto == null) {
            return;
        }
        this.projectRepository.setProject(projectAssembler.fromDto(dto));
    }

    public void setUndoRedoListener(Function<Void, Void> handler) {
        this.undoRedoListener = handler;
    }

    public boolean redoAvailable() {
        return undoRedoManager.redoAvailable();
    }

    public boolean undoAvailable() {
        return undoRedoManager.undoAvailable();
    }

    public List<TileDto> fillSurface(SurfaceDto dto, TileDto masterTileDto, PatternType patternType, SealsInfoDto sealingDto, Double tileAngle, Double tileShifting) {

        List<TileDto> tiles = fillWithoutSaving(dto, masterTileDto, patternType, sealingDto, tileAngle, tileShifting);

        justDoIt();
        return tiles;
    }

    public List<TileDto> fillWithoutSaving(SurfaceDto dto, TileDto masterTileDto, PatternType patternType, SealsInfoDto sealingDto, Double tileAngle, Double tileShifting) {

        Surface desiredSurface = surfaceService.getSurfaceById(dto.id).get();

        Tile masterTile = masterTileDto != null ? tileAssembler.fromDto(masterTileDto) : getDefaultTile();

        SealsInfo sealing = sealingDto != null ? sealingAssembler.fromDto(sealingDto) : getDefaultSealing();
        double angle = tileAngle != null ? tileAngle : 0;
        double shift = tileShifting != null ? tileShifting : 0;
        PatternType pattern = patternType != null ? patternType : PatternType.DEFAULT;

        desiredSurface.fillSurface(masterTile, sealing, pattern, angle, shift);

        SurfaceDto newDto = surfaceAssembler.toDto(desiredSurface);
        this.internalUpdateSurface(newDto); // important

        return newDto.tiles;
    }

    private Tile getDefaultTile() {
        Material material = projectRepository.getProject().getMaterials().get(0);
        List<Point> tileSummits = RectangleHelper.rectangleInfoToSummits(new Point(0, 0), material.getTileTypeWidth(), material.getTileTypeHeight());
        return new Tile(tileSummits, material);
    }

    private SealsInfo getDefaultSealing() {
        return new SealsInfo(0.02, Color.BLUE);
    }

    public void loadProject(String projectPath) {
        try (FileInputStream fi = new FileInputStream(projectPath)) {
            ObjectInputStream os = new ObjectInputStream(fi);
            this.projectRepository.setProject((Project) os.readObject());
            os.close();

            undoRedoManager = new UndoRedoManager();
            justDoIt();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void saveProject(String projectPath) {
        try (FileOutputStream fs = new FileOutputStream(projectPath)) {
            ObjectOutputStream os = new ObjectOutputStream(fs);

            os.writeObject(this.projectRepository.getProject());
            os.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void newProject() {
        this.projectRepository.setProject(new Project());
        this.undoRedoManager = new UndoRedoManager();
    }

    public void fusionSurfaces(List<SurfaceDto> surfacesDto) {
        List<Surface> surfaces = surfacesDto.stream().map(dto -> surfaceService.getSurfaceById(dto.id).get()).collect(Collectors.toList());
        this.projectRepository.getProject().fusionSurfaces(surfaces);

        justDoIt();
    }

    public void unFusionSurface(SurfaceDto surfaceDto) {
        if (!surfaceDto.isFusionned) {
            return;
        }
        FusionnedSurface currentSurface = (FusionnedSurface) this.projectRepository.getProject().getSurfaces().stream().filter(s -> s.getId().isSame(surfaceDto.id)).findFirst().get();
        projectRepository.getProject().unfusionSurfaces(currentSurface);

        justDoIt();
    }

    public void getAccounting() {
        List<Accounting> account = new ArrayList<>();
        for (Material i : this.projectRepository.getProject().getMaterials()) {
            Accounting temp = new Accounting(this.projectRepository.getProject().getSurfaces(), i);
            account.add(temp);
        }
        Maccount = account;
    }

    public void getSurfaceAccount(List<SurfaceDto> pSurface) {
        List<Accounting> account = new ArrayList<>();
        List<Surface> surfaces = pSurface.stream().map(dto -> {
            return this.projectRepository.getProject().getSurfaces().stream().filter(s -> s.getId().isSame(dto.id)).findFirst().get();
        }).collect(Collectors.toList());
        for (Surface i : surfaces) {
            if (i.getTiles().size() > 0) {
                Accounting temp = new Accounting(surfaces, i.getTiles().get(0).getMaterial());
                account.add(temp);
            }
        }
        Maccount = account;
    }

    public String inspectProject(double pWidth, double pHeight) {
        return inspector.inspect(this.projectRepository.getProject(), pWidth, pHeight);
    }

    public Optional<MaterialDto> getMaterialByName(String materialName) {
        Optional<Material> material = this.materialService.getMaterialByName(materialName);
        if (material.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(materialAssembler.toDto(material.get()));
    }

    private void justDoIt() {
        undoRedoManager.justDoIt(projectAssembler.toDto(projectRepository.getProject()));
        if (undoRedoListener != null) {
            undoRedoListener.apply(null);
        }
    }

//    public void debugTileCutting(SurfaceDto surfaceDto, TileDto tileDto) {
//        Surface surface = this.vraiProject.getSurfaces().stream().filter(s -> s.getId().isSame(surfaceDto.id)).findFirst().get();
//        SurfaceAssembler.fromDto(surfaceDto, surface);
//
//        Tile tile = SurfaceAssembler.fromDto(tileDto);
//
//        TileCutter tc = new TileCutter();
//
//        boolean isAllOutside = tc.isAllOutside(surface, tile, false);
//        boolean isAllInside = tc.isAllInside(surface, tile);
//
//        System.out.println(String.format("all outside : %b, all inside : %b", isAllOutside, isAllInside));
//    }
}
