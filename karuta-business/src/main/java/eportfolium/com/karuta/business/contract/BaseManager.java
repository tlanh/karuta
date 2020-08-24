package eportfolium.com.karuta.business.contract;

import eportfolium.com.karuta.model.bean.GroupRights;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.UUID;

public interface BaseManager {
    GroupRights getRights(Long userId, UUID nodeId);

    boolean hasRight(Long userId, UUID nodeId, String right);

    boolean canRead(UserDetails userDetails, UUID nodeId);

    boolean canDelete(UserDetails userDetails, UUID nodeId);

    boolean canWrite(UserDetails userDetails, UUID nodeId);
}
