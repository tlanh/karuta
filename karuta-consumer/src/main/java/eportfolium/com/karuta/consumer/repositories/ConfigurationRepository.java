package eportfolium.com.karuta.consumer.repositories;

import eportfolium.com.karuta.model.bean.Configuration;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConfigurationRepository extends CrudRepository<Configuration, Long> {

}
