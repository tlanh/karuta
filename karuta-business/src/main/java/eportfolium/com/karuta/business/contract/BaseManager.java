package eportfolium.com.karuta.business.contract;

import eportfolium.com.karuta.model.bean.GroupRights;

import java.util.UUID;

public interface BaseManager {
    GroupRights getRights(Long userId, UUID nodeId);

    boolean hasRight(Long userId, UUID nodeId, String right);
}
