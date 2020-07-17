package eportfolium.com.karuta.document;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName("right")
public class RightDocument {
    private boolean RD;
    private boolean WR;
    private boolean SB;
    private boolean DL;

    public RightDocument() { }

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

    public void setRD(boolean read) {
        this.RD = read;
    }

    @JsonGetter("WR")
    public boolean getWR() {
        return WR;
    }

    public void setWR(boolean write) {
        this.WR = write;
    }

    @JsonGetter("SB")
    public boolean getSB() {
        return SB;
    }

    public void setSB(boolean submit) {
        this.SB = submit;
    }

    @JsonGetter("DL")
    public boolean getDL() {
        return DL;
    }

    public void setDL(boolean delete) {
        this.DL = delete;
    }
}
