package application;

import Domain.Material;

import java.util.List;
import java.util.Optional;

public class MaterialService {

    ProjectRepository projectRepository;

    public MaterialService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public Optional<Material> getMaterialByName(String name) {
        for(Material m : projectRepository.getProject().getMaterials()) {
            if(m.getMaterialName().equals(m.getMaterialName())) {
                return Optional.of(m);
            }
        }
        return Optional.empty();
    }

    public void updateMaterial(MaterialDto materialDto) {
        List<Material> allMaterials = projectRepository.getProject().getMaterials();
        for (Material material : allMaterials) {
            if(material.getMaterialName().equals(materialDto.name)) {
                MaterialAssembler.fromDto(materialDto, material);
            }
        }
    }
}
