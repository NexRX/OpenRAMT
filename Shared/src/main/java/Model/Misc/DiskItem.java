package Model.Misc;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DiskItem implements Serializable {
    private final String diskIdentifier;
    private final long space;
    private final long capacity;
    private final long diskIO;

    public DiskItem(String diskIdentifier, long space, long capacity, long diskIO) {
        this.diskIdentifier = diskIdentifier;
        this.space = space;
        this.capacity = capacity;
        this.diskIO = diskIO;
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

    public long getDiskIO() { return diskIO;  }

}
