package eportfolium.com.karuta.business.contract;

import eportfolium.com.karuta.model.bean.GroupRights;

import java.util.UUID;

public interface BaseManager {
    GroupRights getRights(Long userId, Long groupId, UUID nodeId);

    boolean hasRight(Long userId, Long groupId, UUID nodeId, String right);
}
