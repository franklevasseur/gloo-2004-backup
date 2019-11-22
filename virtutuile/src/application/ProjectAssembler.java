package application;

import Domain.Project;

import java.util.stream.Collectors;

public class ProjectAssembler {

    public static ProjectDto toDto(Project project) {

        ProjectDto dto = new ProjectDto();

        dto.materials = project.getMaterials().stream().map(s -> MaterialAssembler.toDto(s)).collect(Collectors.toList());
        dto.surfaces = project.getSurfaces().stream().map(s -> SurfaceAssembler.toDto(s)).collect(Collectors.toList());
        dto.fusionnedSurfaces = project.getFusionnedSurfaces().stream().map(fs -> {
            FusionnedSurfaceDto fsDto = new FusionnedSurfaceDto();
            fsDto.fusionnedSurfaces = fs.getFusionnedSurfaces().stream().map(s -> SurfaceAssembler.toDto(s)).collect(Collectors.toList());
            return fsDto;
        }).collect(Collectors.toList());

        return dto;
    }
}
