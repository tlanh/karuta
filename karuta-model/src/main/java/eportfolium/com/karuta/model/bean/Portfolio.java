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
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "portfolio")
@EntityListeners(AuditListener.class)
public class Portfolio implements Serializable {

	private static final long serialVersionUID = 6664845320002538859L;

	private UUID id;
	private Node rootNode;
	private Credential credential;
	private UUID modelId;
	private Long modifUserId;
	private Date modifDate;
	private int active;

	private Set<GroupRightInfo> groupRightInfo = new HashSet<GroupRightInfo>(0);
	private Set<Node> nodes = new HashSet<Node>(0);

	public Portfolio() {
	}

	public Portfolio(Portfolio original) {
		this.rootNode = original.getRootNode();
		this.credential = original.getCredential() != null ? new Credential(original.getCredential().getId()) : null;
		this.modelId = original.getModelId();
		this.modifUserId = original.getModifUserId();
		this.modifDate = original.getModifDate() != null ? new Date(original.getModifDate().getTime()) : null;
		this.active = original.getActive();
	}

	public Portfolio(UUID id) {
		this.id = id;
	}

	public Portfolio(UUID id, Credential credential, Long modifUserId, int active) {
		this.id = id;
		this.credential = credential;
		this.modifUserId = modifUserId;
		this.active = active;
	}

	public Portfolio(UUID id, Node rootNode, Credential credential, UUID modelId, Long modifUserId, Date modifDate,
			int active) {
		this.id = id;
		this.rootNode = rootNode;
		this.credential = credential;
		this.modelId = modelId;
		this.modifUserId = modifUserId;
		this.modifDate = modifDate;
		this.active = active;
	}

	@Id
	@GenericGenerator(name = "uuid2", strategy = "uuid2")
	@GeneratedValue(generator = "uuid2")
	@Column(name = "portfolio_id", unique = true, nullable = false, columnDefinition = "BINARY(16)")
	public UUID getId() {
		return this.id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "root_node_uuid")
	public Node getRootNode() {
		return this.rootNode;
	}

	public void setRootNode(Node rootNode) {
		this.rootNode = rootNode;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	public Credential getCredential() {
		return this.credential;
	}

	public void setCredential(Credential credential) {
		if (this.credential != null) {
			/* Maintain the bidirectional relationship with my parent, User. */
			this.credential.internalRemovePortfolio(this);
		}
		this.credential = credential;
		if (credential != null) {
			/* Maintain the bidirectional relationship with my parent, User. */
			credential.internalAddPortfolio(this);
		}
	}

	@Column(name = "model_id", columnDefinition = "BINARY(16)")
	public UUID getModelId() {
		return this.modelId;
	}

	public void setModelId(UUID modelId) {
		this.modelId = modelId;
	}

	@Column(name = "modif_user_id", nullable = false)
	public Long getModifUserId() {
		return this.modifUserId;
	}

	public void setModifUserId(Long modifUserId) {
		this.modifUserId = modifUserId;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "modif_date", length = 19)
	public Date getModifDate() {
		return this.modifDate;
	}

	public void setModifDate(Date modifDate) {
		this.modifDate = modifDate;
	}

	@Column(name = "active", nullable = false)
	public int getActive() {
		return this.active;
	}

	public void setActive(int active) {
		this.active = active;
	}

	@OneToMany(mappedBy = "portfolio", fetch = FetchType.LAZY)
	public Set<Node> getNodes() {
		return nodes;
	}

	public void setNodes(Set<Node> nodes) {
		this.nodes = nodes;
	}

	@OneToMany(mappedBy = "portfolio", fetch = FetchType.LAZY)
	public Set<GroupRightInfo> getGroupRightInfo() {
		return groupRightInfo;
	}

	public void setGroupRightInfo(Set<GroupRightInfo> groupRightInfo) {
		this.groupRightInfo = groupRightInfo;
	}

	public void addNode(Node node) {
		node.setPortfolio(this);
	}

	/**
	 * Beware - in JPA to persist this change you must merge or remove the child.
	 * Merging the parent will not cascade to the child because it no longer has a
	 * reference to the child.
	 */
	public void removeNode(Node node) {
		node.setPortfolio(null);
	}

	public void internalAddNode(Node node) {
		nodes.add(node);
	}

	/**
	 * Beware - in JPA to persist this change you must merge or remove the child.
	 * Merging the parent will not cascade to the child because it no longer has a
	 * reference to the child.
	 */
	public void internalRemoveNode(Node node) {
		nodes.remove(node);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Portfolio other = (Portfolio) obj;
		if (getId() == null) {
			if (other.getId() != null)
				return false;
		} else if (!getId().equals(other.getId()))
			return false;
		return true;
	}

}
