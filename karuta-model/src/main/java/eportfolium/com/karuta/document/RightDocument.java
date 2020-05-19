package eportfolium.com.karuta.document;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName("right")
public class RightDocument {
    private boolean RD;
    private boolean WR;
    private boolean SB;
    private boolean DL;

    public RightDocument(boolean read, boolean write, boolean submit, boolean delete) {
        this.RD = read;
        this.WR = write;
        this.SB = submit;
        this.DL = delete;
    }

    @JsonGetter("RD")
    public boolean getRD() {
        return RD;
    }

    @JsonGetter("WR")
    public boolean getWR() {
        return WR;
    }

    @JsonGetter("SB")
    public boolean getSB() {
        return SB;
    }

    @JsonGetter("DL")
    public boolean getDL() {
        return DL;
    }
}
