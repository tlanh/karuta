package eportfolium.com.karuta.document;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import eportfolium.com.karuta.model.bean.GroupRights;

import java.util.UUID;

@JsonRootName("groupRights")
public class GroupRightsDocument {

    private Long gid;
    private Long templateId;
    private Item item;

    public GroupRightsDocument(GroupRights groupRights) {
        this.gid = groupRights.getGroupRightInfo().getGroupInfo().getId();
        this.templateId = groupRights.getGroupRightInfo().getId();
        this.item = new Item(groupRights);
    }

    @JsonGetter("gid")
    @JacksonXmlProperty(isAttribute = true)
    public Long getGid() {
        return gid;
    }

    @JsonGetter("templateId")
    @JacksonXmlProperty(isAttribute = true)
    public Long getTemplateId() {
        return templateId;
    }

    @JsonGetter("item")
    public Item getItem() {
        return item;
    }

    @JsonRootName("item")
    public class Item {
        private UUID id;

        private boolean add;
        private boolean del;
        private boolean read;
        private boolean write;
        private boolean submit;

        private Long creator;
        private Long owner;

        private String typeId;

        public Item(GroupRights groupRights) {
            this.id = groupRights.getGroupRightsId();

            this.add = groupRights.isAdd();
            this.del = groupRights.isDelete();
            this.read = groupRights.isRead();
            this.write = groupRights.isWrite();
            this.submit = groupRights.isSubmit();

            this.creator = groupRights.getGroupRightInfo().getGroupInfo().getOwner();
            this.owner = groupRights.getGroupRightInfo().getOwner();

            this.typeId = groupRights.getTypesId();
        }

        @JsonGetter("id")
        @JacksonXmlProperty(isAttribute = true)
        public UUID getId() {
            return id;
        }

        @JsonGetter("add")
        @JacksonXmlProperty(isAttribute = true)
        public boolean getAdd() {
            return add;
        }

        @JsonGetter("del")
        @JacksonXmlProperty(isAttribute = true)
        public boolean getDel() {
            return del;
        }

        @JsonGetter("read")
        @JacksonXmlProperty(isAttribute = true)
        public boolean getRead() {
            return read;
        }

        @JsonGetter("write")
        @JacksonXmlProperty(isAttribute = true)
        public boolean getWrite() {
            return write;
        }

        @JsonGetter("submit")
        @JacksonXmlProperty(isAttribute = true)
        public boolean getSubmit() {
            return submit;
        }

        @JsonGetter("creator")
        @JacksonXmlProperty(isAttribute = true)
        public Long getCreator() {
            return creator;
        }

        @JsonGetter("owner")
        @JacksonXmlProperty(isAttribute = true)
        public Long getOwner() {
            return owner;
        }

        @JsonGetter("typeId")
        @JacksonXmlProperty(isAttribute = true)
        public String getTypeId() {
            return typeId;
        }
    }
}
