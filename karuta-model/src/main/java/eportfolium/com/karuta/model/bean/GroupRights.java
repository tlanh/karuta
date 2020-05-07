/* =======================================================
	Copyright 2020 - ePortfolium - Licensed under the
	Educational Community License, Version 2.0 (the "License"); you may
	not use this file except in compliance with the License. You may
	obtain a copy of the License at

	http://www.osedu.org/licenses/ECL-2.0

	Unless required by applicable law or agreed to in writing,
	software distributed under the License is distributed on an "AS IS"
	BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
	or implied. See the License for the specific language governing
	permissions and limitations under the License.
   ======================================================= */

package eportfolium.com.karuta.model.bean;

import java.io.Serializable;
import java.util.UUID;

import javax.persistence.AssociationOverride;
import javax.persistence.AssociationOverrides;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "group_rights")
@AssociationOverrides({
		@AssociationOverride(name = "id.groupRightInfo", joinColumns = @JoinColumn(name = "grid", nullable = false)) })
public class GroupRights implements Serializable {

	private static final long serialVersionUID = 7563709378804723952L;

	public static final String NONE = "none";
	public static final String ADD = "add";
	public static final String READ = "read";
	public static final String WRITE = "write";
	public static final String SUBMIT = "submit";
	public static final String DELETE = "delete";
	public static final String LIER = "lier";
	public static final String DELIER = "delier";

	private GroupRightsId id;
	private boolean read;
	private boolean write;
	private boolean delete;
	private boolean submit;
	private boolean add;
	private String typesId;
	private String rulesId;
	private String notifyRoles;

	public GroupRights() {
	}

	public GroupRights(boolean read, boolean write, boolean delete, boolean submit) {
		this.read = read;
		this.write = write;
		this.delete = delete;
		this.submit = submit;
	}

	public GroupRights(GroupRightsId id, boolean read, boolean write, boolean delete, boolean submit, boolean add) {
		this.id = id;
		this.read = read;
		this.write = write;
		this.delete = delete;
		this.submit = submit;
		this.add = add;
	}

	public GroupRights(GroupRightsId id, boolean read, boolean write, boolean delete, boolean submit, boolean ad,
			String typesId, String rulesId, String notifyRoles) {
		this.id = id;
		this.read = read;
		this.write = write;
		this.delete = delete;
		this.submit = submit;
		this.add = ad;
		this.typesId = typesId;
		this.rulesId = rulesId;
		this.notifyRoles = notifyRoles;
	}

	public GroupRights(GroupRights rights) {
		this.id = new GroupRightsId();
		this.read = rights.isRead();
		this.write = rights.isWrite();
		this.delete = rights.isDelete();
		this.submit = rights.isSubmit();
		this.add = rights.isAdd();
		this.typesId = rights.getTypesId();
		this.rulesId = rights.getRulesId();
	}

	@EmbeddedId
	public GroupRightsId getId() {
		return this.id;
	}

	@Transient
	public GroupRightInfo getGroupRightInfo() {
		return getId().getGroupRightInfo();
	}

	public void setGroupRightInfo(GroupRightInfo groupRightInfo) {
		getId().setGroupRightInfo(groupRightInfo);
	}

	@Transient
	public UUID getGroupRightsId() {
		return getId().getId();
	}

	public void setGroupRightsId(UUID id) {
		getId().setId(id);
	}

	public void setId(GroupRightsId id) {
		this.id = id;
	}

	@Column(name = "RD", nullable = false)
	public boolean isRead() {
		return this.read;
	}

	public void setRead(boolean rd) {
		this.read = rd;
	}

	@Column(name = "WR", nullable = false)
	public boolean isWrite() {
		return this.write;
	}

	public void setWrite(boolean wr) {
		this.write = wr;
	}

	@Column(name = "DL", nullable = false)
	public boolean isDelete() {
		return this.delete;
	}

	public void setDelete(boolean dl) {
		this.delete = dl;
	}

	@Column(name = "SB", nullable = false)
	public boolean isSubmit() {
		return this.submit;
	}

	public void setSubmit(boolean sb) {
		this.submit = sb;
	}

	@Column(name = "AD", nullable = false)
	public boolean isAdd() {
		return this.add;
	}

	public void setAdd(boolean add) {
		this.add = add;
	}

	@Lob
	@Column(name = "types_id")
	public String getTypesId() {
		return this.typesId;
	}

	public void setTypesId(String typesId) {
		this.typesId = typesId;
	}

	@Lob
	@Column(name = "rules_id")
	public String getRulesId() {
		return this.rulesId;
	}

	public void setRulesId(String rulesId) {
		this.rulesId = rulesId;
	}

	@Lob
	@Column(name = "notify_roles")
	public String getNotifyRoles() {
		return this.notifyRoles;
	}

	public void setNotifyRoles(String notifyRoles) {
		this.notifyRoles = notifyRoles;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		GroupRights that = (GroupRights) o;

		if (getId() != null ? !getId().equals(that.getId()) : that.getId() != null)
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		return (getId() != null ? getId().hashCode() : 0);
	}

}
