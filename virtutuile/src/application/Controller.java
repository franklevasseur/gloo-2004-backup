package application;

import Domain.*;
import utils.*;
import utils.Point;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Controller {

    private static Controller instance = new Controller();
    public static Controller getInstance() {
        return instance;
    }
    private UndoRedoManager undoRedoManager = new UndoRedoManager();

    public List<Accounting> Maccount  = new ArrayList<>();

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

    public void updateMaterial(MaterialDto materialDto){

        List<Material> allMaterials = vraiProject.getMaterials();
        for (Material material : allMaterials){
            if(material.getMaterialName() == materialDto.name){
                material.setColor(materialDto.color);
                material.setCostPerBox(materialDto.costPerBox);
                material.setNbTilePerBox(materialDto.nbTilePerBox);
                material.setTileTypeHeight(materialDto.tileTypeHeight);
                material.setTileTypeWidth(materialDto.tileTypeWidth);
//                MaterialAssembler.fromDto(materialDto);
            }
        }


        //TODO undo/redo manager ?
    }

    private void internalUpdateSurface(SurfaceDto surfaceDto) {
        Surface surface = this.vraiProject.getSurfaces().stream().filter(s -> s.getId().isSame(surfaceDto.id)).findFirst().get();
        SurfaceAssembler.fromDto(surfaceDto, surface);
    }

    // atomic move and fill or resize and fill
    public List<TileDto> updateAndRefill(SurfaceDto dto, application.TileDto masterTile, PatternType patternDto, SealsInfoDto sealing, double angle, double shift)  {
        internalUpdateSurface(dto);
        List<TileDto> tiles = fillSurface(dto, masterTile, patternDto, sealing, angle, shift);
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

    public List<TileDto> fillSurface(SurfaceDto dto, TileDto masterTileDto, PatternType patternType, SealsInfoDto sealingDto, Double tileAngle, Double tileShifting) {

        Surface desiredSurface = this.vraiProject.getSurfaces().stream().filter(s -> s.getId().isSame(dto.id)).findFirst().get();
        Tile masterTile = masterTileDto != null ? SurfaceAssembler.fromDto(masterTileDto) : getDefaultTile();
        SealsInfo sealing = sealingDto != null ? SurfaceAssembler.fromDto(sealingDto) : getDefaultSealing();
        double angle = tileAngle != null ? tileAngle : 0;
        double shift = tileShifting != null ? tileShifting : 0;
        PatternType pattern = patternType != null ? patternType : PatternType.DEFAULT;

        desiredSurface.fillSurface(masterTile, sealing, pattern, angle, shift);

        SurfaceDto newDto = SurfaceAssembler.toDto(desiredSurface);

        this.internalUpdateSurface(newDto); // important
        undoRedoManager.justDoIt(ProjectAssembler.toDto(vraiProject));

        return newDto.tiles;
    }

    private Tile getDefaultTile() {
        Material material = vraiProject.getMaterials().get(0);
        List<Point> tileSummits = RectangleHelper.rectangleInfoToSummits(new Point(0, 0), material.getTileTypeWidth(), material.getTileTypeHeight());
        return new Tile(tileSummits, material);
    }

    private SealsInfo getDefaultSealing() {
        return new SealsInfo(0.02, Color.BLUE);
    }

    public void loadProject(String projectPath) {
        try(FileInputStream fi = new FileInputStream(projectPath)){
            ObjectInputStream os = new ObjectInputStream(fi);
            this.vraiProject = (Project) os.readObject();

            os.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void saveProject(String projectPath) {
        try(FileOutputStream fs = new FileOutputStream(projectPath)){
            ObjectOutputStream os = new ObjectOutputStream(fs);

            os.writeObject(this.vraiProject);
            os.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void newProject(){
        this.vraiProject = new Project();
        this.undoRedoManager = new UndoRedoManager();
        MaterialDto defaultNewMaterial = new MaterialDto();
        defaultNewMaterial.materialType = MaterialType.tileMaterial;
        defaultNewMaterial.name = "Melon d'eau";
        defaultNewMaterial.color = Color.GREEN;
        defaultNewMaterial.costPerBox = 50.0;
        defaultNewMaterial.tileTypeHeight = 0.3;
        defaultNewMaterial.tileTypeWidth = 0.6;
        defaultNewMaterial.nbTilePerBox = 45;
        this.createMaterial(defaultNewMaterial);
        undoRedoManager.justDoIt(ProjectAssembler.toDto(vraiProject));
    }

    public void fusionSurfaces(List<SurfaceDto> surfacesDto) {
        List<Surface> surfaces = surfacesDto.stream().map(dto -> {
            return this.vraiProject.getSurfaces().stream().filter(s -> s.getId().isSame(dto.id)).findFirst().get();
        }).collect(Collectors.toList());
        this.vraiProject.fusionSurfaces(surfaces);
        undoRedoManager.justDoIt(ProjectAssembler.toDto(vraiProject));
    }

    public void unFusionSurface(SurfaceDto surfaceDto) {
        if (!surfaceDto.isFusionned) {
            return;
        }
        FusionnedSurface currentSurface = (FusionnedSurface) this.vraiProject.getSurfaces().stream().filter(s -> s.getId().isSame(surfaceDto.id)).findFirst().get();
        vraiProject.unfusionSurfaces(currentSurface);
        undoRedoManager.justDoIt(ProjectAssembler.toDto(vraiProject));
    }

    public void createMaterial(MaterialDto dto) {
        Material material = MaterialAssembler.fromDto(dto);
        vraiProject.getMaterials().add(material);
        // TODO: avertir undo/redo que ca vient de se passer (Philippe ne pas enlever ce todo, c'est pour Frank)
    }

    public void getAccounting(){
        List<Accounting> account = new ArrayList<>();
        for (Material i: this.vraiProject.getMaterials()) {
            Accounting temp = new Accounting(this.vraiProject.getSurfaces(), i);
            account.add(temp);
        }
        Maccount = account;
    }

    public void getSurfaceAccount(List<SurfaceDto> pSurface){
        List<Accounting> account = new ArrayList<>();
        List<Surface> surfaces = pSurface.stream().map(dto -> {
            return this.vraiProject.getSurfaces().stream().filter(s -> s.getId().isSame(dto.id)).findFirst().get();
        }).collect(Collectors.toList());
        for(Surface i: surfaces){
            if (i.getTiles().size() > 0) {
                Accounting temp = new Accounting(surfaces, i.getTiles().get(0).getMaterial());
                account.add(temp);
            }
        }
        Maccount = account;

    }

    public String inspectProject(double pWidth, double pHeight) {
        pairResult temp = inspector.inspect(this.vraiProject, pWidth, pHeight);
        vraiProject = temp.project;
        undoRedoManager.justDoIt(ProjectAssembler.toDto(vraiProject));
        return temp.message;
    }

    public String inspectSurface(SurfaceDto dto, double pWidth, double pHeight) {
        // TODO: Cette methode n'est pas linké avec le ui
        Surface desiredSurface = this.vraiProject.getSurfaces().stream().filter(s -> s.getId().isSame(dto.id)).findFirst().get();
        return inspector.inspect(desiredSurface, pWidth, pHeight);
    }
    public Material getSelectedMaterial(MaterialDto materialDto){
        Material material = new Material(materialDto.color,MaterialType.tileMaterial,materialDto.name);
        for(Material m:vraiProject.getMaterials()){
            if(m.getMaterialName().equals(material.getMaterialName())){
                material = m;
            }
        }
        return material;
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
