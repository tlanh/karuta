package eportfolium.com.karuta.document;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import eportfolium.com.karuta.model.bean.GroupRights;

import java.util.UUID;

@JsonRootName("node")
public class NodeRightsDocument {
    private final UUID uuid;
    private final RoleElement role;

    private static class RoleElement {
        private final GroupRights groupRights;

        private static class RightsElement {
            private final boolean read, write, delete, submit;

            public RightsElement(GroupRights groupRights) {
                this.read = groupRights.isRead();
                this.write = groupRights.isWrite();
                this.delete = groupRights.isDelete();
                this.submit = groupRights.isSubmit();
            }

            @JacksonXmlProperty(isAttribute = true)
            public boolean RD() { return read; }

            @JacksonXmlProperty(isAttribute = true)
            public boolean WR() { return write; }

            @JacksonXmlProperty(isAttribute = true)
            public boolean DL() { return delete; }

            @JacksonXmlProperty(isAttribute = true)
            public boolean SB() { return submit; }
        }

        public RoleElement(GroupRights groupRights) {
            this.groupRights = groupRights;
        }

        @JacksonXmlProperty(isAttribute = true)
        public String getName() {
            return groupRights.getGroupRightInfo().getLabel();
        }

        public RightsElement getRights() {
            return new RightsElement(groupRights);
        }
    }

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
