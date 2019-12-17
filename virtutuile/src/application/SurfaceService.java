package application;

import Domain.Material;
import Domain.Surface;
import Domain.Tile;
import utils.Id;
import utils.RectangleHelper;
import utils.RectangleInfo;

import java.util.Optional;

public class SurfaceService {

    private ProjectRepository projectRepository;

    public SurfaceService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public Optional<Surface> getSurfaceById(Id surfaceId) {
        return this.projectRepository.getProject().getSurfaces().stream().filter(s -> s.getId().isSame(surfaceId)).findFirst();
    }

    public void removeSurfaceById(Id surfaceId) {
        Optional<Surface> surface = this.getSurfaceById(surfaceId);
        if (surface.isPresent()) {
            this.projectRepository.getProject().removeSurface(surface.get());
        }
    }

    public void updateMaterial(Material material) {

        for (Surface s : projectRepository.getProject().getSurfaces()) {
            Tile masterTile = s.getMasterTile();
            if (masterTile == null
                    || !masterTile.getMaterial().getMaterialName().equals(material.getMaterialName())) {
                continue;
            }

            RectangleInfo masterTileInfo = RectangleHelper.summitsToRectangleInfo(masterTile.getSummits());
            double materialWidth = material.getTileTypeWidth();
            double materialHeight = material.getTileTypeHeight();
            masterTileInfo.width = materialWidth;
            masterTileInfo.height = materialHeight;
            masterTile.setSummits(RectangleHelper.rectangleInfoToSummits(masterTileInfo));
            masterTile.setMaterial(material);
            s.refillFromCurrentInfo();
        }
    }
}
