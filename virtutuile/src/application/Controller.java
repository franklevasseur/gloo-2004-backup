package application;

import domain.SurfaceId;
import gui.RectangleSurfaceUI;

public class Controller {

    private static Controller instance = new Controller();
    public static Controller getInstance() {
        return instance;
    }

    public SurfaceId createSurface(RectangleSurfaceUI surface) {

        // communicate surface creation with domain layers

        return new SurfaceId();
    }

    public void updateSurface(RectangleSurfaceUI surface) {
        // communicate surface update with domain layers
    }
}
