package application;

import Domain.Project;
import Domain.Surface;

import java.util.List;
import java.util.stream.Collectors;

public class ProjectAssembler {

    private SurfaceAssembler surfaceAssembler;
    private MaterialAssembler materialAssembler;

    public ProjectAssembler(SurfaceAssembler surfaceAssembler, MaterialAssembler materialAssembler) {
        this.materialAssembler = materialAssembler;
        this.surfaceAssembler = surfaceAssembler;
    }

    public ProjectDto toDto(Project project) {

        ProjectDto dto = new ProjectDto();

        dto.materials = project.getMaterials().stream().map(s -> materialAssembler.toDto(s)).collect(Collectors.toList());
        dto.surfaces = project.getSurfaces().stream().map(s -> surfaceAssembler.toDto(s)).collect(Collectors.toList());

        return dto;
    }

    public Project fromDto(ProjectDto dto) {

        Project project = new Project();

        List<Surface> surfaces = dto.surfaces.stream().map(s -> surfaceAssembler.fromDto(s)).collect(Collectors.toList());
        project.setSurfaces(surfaces);

        project.setMaterials(dto.materials.stream().map(m -> materialAssembler.fromDto(m)).collect(Collectors.toList()));

        return project;
    }
}
