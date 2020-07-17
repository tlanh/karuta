package eportfolium.com.karuta.consumer.repositories;

import eportfolium.com.karuta.model.bean.CredentialGroup;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CredentialGroupRepository extends CrudRepository<CredentialGroup, Long> {
    CredentialGroup findByLabel(String label);
}
