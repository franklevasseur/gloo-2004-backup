package utils;

public class Id {

    private static int instanceCount = 0;
    private int id;

    public Id() {
        // TODO: this logic won't work if a file database is used. Need a to generate uniq ids...
        id = instanceCount;
        instanceCount++;
    }

    public boolean isSame(Id other) {
        return this.id == other.id;
    }

    public String getValue() {
        return String.valueOf(this.id);
    }
}
