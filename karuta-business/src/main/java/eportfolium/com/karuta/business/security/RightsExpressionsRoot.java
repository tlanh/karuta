package eportfolium.com.karuta.business.security;

import eportfolium.com.karuta.business.UserInfo;
import eportfolium.com.karuta.business.contract.NodeManager;
import eportfolium.com.karuta.consumer.repositories.NodeRepository;
import eportfolium.com.karuta.model.bean.GroupRights;
import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.core.Authentication;

import java.util.UUID;

public class RightsExpressionsRoot extends SecurityExpressionRoot implements MethodSecurityExpressionOperations  {
    private Object filterObject;
    private Object returnObject;

    private final Long userId;

    private final NodeRepository nodeRepository;
    private final NodeManager nodeManager;

    public RightsExpressionsRoot(Authentication authentication, NodeRepository nodeRepository, NodeManager nodeManager) {
        super(authentication);

        this.nodeRepository = nodeRepository;
        this.nodeManager = nodeManager;

        if (this.getPrincipal() instanceof UserInfo)
            this.userId = ((UserInfo) this.getPrincipal()).getId();
        else
            this.userId = 0L;
    }

    /**
     * Checks whether a node is public or not.
     */
    public boolean isPublic(UUID nodeId) {
        return nodeRepository.isPublic(nodeId);
    }

    /**
     * Checks whether a user has the permission to read a node.
     */
    public boolean canRead(UUID nodeId) {
        final GroupRights rights = nodeManager.getRights(userId, nodeId);

        return rights.isRead();
    }

    /**
     * Checks whether a user has the permission to delete a node.
     */
    public boolean canDelete(UUID nodeId) {
        final GroupRights rights = nodeManager.getRights(userId, nodeId);

        return rights.isDelete();
    }

    /**
     * Checks whether a user has the permission to write on a node.
     */
    public boolean canWrite(UUID nodeId) {
        final GroupRights rights = nodeManager.getRights(userId, nodeId);

        return rights.isWrite();
    }

    @Override
    public void setFilterObject(Object filterObject) {
        System.out.println(filterObject);
        this.filterObject = filterObject;
    }

    @Override
    public Object getFilterObject() {
        return filterObject;
    }

    @Override
    public void setReturnObject(Object returnObject) {
        this.returnObject = returnObject;
    }

    @Override
    public Object getReturnObject() {
        return returnObject;
    }

    @Override
    public Object getThis() {
        return this;
    }
}
