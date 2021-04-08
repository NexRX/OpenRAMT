package Model.Misc;

import java.io.Serializable;

public class HardDiskItem implements Serializable {
    private final String diskIdentifier;
    private final long diskIO;

    public HardDiskItem(String diskIdentifier, long diskIO) {
        this.diskIdentifier = diskIdentifier;
        this.diskIO = diskIO;
    }

    public String getDiskIdentifier() {
        return diskIdentifier;
    }

    public long getDiskIO() { return diskIO;  }

}
