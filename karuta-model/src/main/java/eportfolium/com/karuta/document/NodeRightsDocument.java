package eportfolium.com.karuta.document;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import eportfolium.com.karuta.model.bean.GroupRights;

import java.util.UUID;

@JsonRootName("node")
public class NodeRightsDocument {
    private UUID uuid;
    private RoleElement role;

    public static class RoleElement {
        private RightElement right;
        private String name;

        @JsonPropertyOrder({"RD", "WR", "DL", "SB"})
        public static class RightElement {
            private boolean RD;
            private boolean WR;
            private boolean DL;
            private boolean SB;

            public RightElement() { }

            public RightElement(GroupRights groupRights) {
                this.RD = groupRights.isRead();
                this.WR = groupRights.isWrite();
                this.DL = groupRights.isDelete();
                this.SB = groupRights.isSubmit();
            }

            @JacksonXmlProperty(localName = "RD", isAttribute = true)
            public boolean getRD() { return RD; }

            @JacksonXmlProperty(localName = "WR", isAttribute = true)
            public boolean getWR() { return WR; }

            @JacksonXmlProperty(localName = "DL", isAttribute = true)
            public boolean getDL() { return DL; }

            @JacksonXmlProperty(localName = "SB", isAttribute = true)
            public boolean getSB() { return SB; }

            public void setRD(boolean RD) { this.RD = RD; }
            public void setWR(boolean WR) { this.WR = WR; }
            public void setDL(boolean DL) { this.DL = DL; }
            public void setSB(boolean SB) { this.SB = SB; }
        }

        public RoleElement() { }

        public RoleElement(GroupRights groupRights) {
            this.right = new RightElement(groupRights);
            this.name = groupRights.getGroupRightInfo().getLabel();
        }

        @JacksonXmlProperty(isAttribute = true)
        public String getName() {
            return name;
        }

        public RightElement getRight() {
            return right;
        }
    }

    public NodeRightsDocument() { }

    public NodeRightsDocument(UUID uuid, GroupRights groupRights) {
        this.uuid = uuid;
        this.role = new RoleElement(groupRights);
    }

    @JacksonXmlProperty(isAttribute = true)
    public UUID getUuid() {
        return uuid;
    }

    public RoleElement getRole() {
        return role;
    }
}
