package utils;

import java.io.Serializable;
import java.util.UUID;

public class Id implements Serializable {

    private UUID id;

    public Id() {
        id = UUID.randomUUID();
    }

    public boolean isSame(Id other) {
        return this.id.equals(other.id);
    }
}
