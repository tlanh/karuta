package eportfolium.com.karuta.model.bean;
// Generated 13 juin 2019 19:14:13 by Hibernate Tools 5.2.10.Final

import java.io.Serializable;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;

/**
 * @author mlengagne
 *
 *         Karuta enables you to give your users certain privileges, by
 *         assigning them to groups. You can create as many users groups as
 *         needed, and assign a user to as many groups as you like.
 */
@Entity
@Table(name = "group_user")
public class GroupUser implements Serializable {

	private static final long serialVersionUID = -1516598438094285942L;

	private GroupUserId id;
	private GroupInfo groupInfo;
	private Credential credential;

	public GroupUser() {
	}

	public GroupUser(GroupUserId id) {
		this.id = id;
	}

	@EmbeddedId
	public GroupUserId getId() {
		return this.id;
	}

	public void setId(GroupUserId id) {
		this.id = id;
	}

//	@AssociationOverride(name = "id.groupInfo", joinColumns = ),
//	@AssociationOverride(name = "id.credential", joinColumns = ) })
//
	@ManyToOne
	@MapsId("groupInfoId")
	@JoinColumn(name = "gid", nullable = false)
	public GroupInfo getGroupInfo() {
		return groupInfo;
	}

	public void setGroupInfo(GroupInfo groupInfo) {
		this.groupInfo = groupInfo;
	}

	@ManyToOne
	@MapsId("credentialId")
	@JoinColumn(name = "userid", nullable = false)
	public Credential getCredential() {
		return credential;
	}

	public void setCredential(Credential credential) {
		this.credential = credential;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		GroupUser that = (GroupUser) o;

		if (getId() != null ? !getId().equals(that.getId()) : that.getId() != null)
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		return (getId() != null ? getId().hashCode() : 0);
	}

}
