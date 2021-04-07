package Model.Misc;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SoftDiskItem implements Serializable {
    private final String diskIdentifier;
    private final long space;
    private final long capacity;

    public SoftDiskItem(String diskIdentifier, long space, long capacity) {
        this.diskIdentifier = diskIdentifier;
        this.space = space;
        this.capacity = capacity;
    }

    public String getDiskIdentifier() {
        return diskIdentifier;
    }

    public long getSpace() {
        return space;
    }

    public long getCapacity() {
        return capacity;
    }

}
