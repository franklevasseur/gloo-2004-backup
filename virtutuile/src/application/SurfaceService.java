package application;

import Domain.Surface;
import utils.Id;

import java.util.Optional;

public class SurfaceService {

    private ProjectRepository projectRepository;

    public SurfaceService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public Optional<Surface> getSurfaceById(Id surfaceId) {
        return this.projectRepository.getProject().getSurfaces().stream().filter(s -> s.getId().isSame(surfaceId)).findFirst();
    }
}
