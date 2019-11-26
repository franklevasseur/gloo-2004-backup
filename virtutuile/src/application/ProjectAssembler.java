package application;

import Domain.Project;
import Domain.Surface;

import java.util.List;
import java.util.stream.Collectors;

public class ProjectAssembler {

    public static ProjectDto toDto(Project project) {

        ProjectDto dto = new ProjectDto();

        dto.materials = project.getMaterials().stream().map(s -> MaterialAssembler.toDto(s)).collect(Collectors.toList());
        dto.surfaces = project.getSurfaces().stream().map(s -> SurfaceAssembler.toDto(s)).collect(Collectors.toList());

        return dto;
    }

    public static Project fromDto(ProjectDto dto) {

        Project project = new Project();

        List<Surface> surfaces = dto.surfaces.stream().map(s -> SurfaceAssembler.fromDto(s)).collect(Collectors.toList());
        project.setSurfaces(surfaces);

        project.setMaterials(dto.materials.stream().map(m -> MaterialAssembler.fromDto(m)).collect(Collectors.toList()));

        return project;
    }
}
