package application;

import Domain.Material;

import java.util.Optional;

public class MaterialService {

    ProjectRepository projectRepository;

    public MaterialService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public Optional<Material> getMaterialByName(String name) {
        for (Material m : projectRepository.getProject().getMaterials()) {
            if (m.getMaterialName().equals(name)) {
                return Optional.of(m);
            }
        }
        return Optional.empty();
    }
}
