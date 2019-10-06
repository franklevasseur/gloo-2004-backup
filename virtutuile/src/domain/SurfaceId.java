package domain;

public class SurfaceId {

    private static int instanceCount;
    private int id;

    public SurfaceId() {
        id = instanceCount;
        instanceCount++;
    }

    public boolean isSame(SurfaceId other) {
        return this.id == other.id;
    }
}
