package eportfolium.com.karuta.consumer.repositories;

import eportfolium.com.karuta.model.bean.Configuration;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface ConfigurationRepository extends CrudRepository<Configuration, Long> {
    Optional<Configuration> findByName(String name);

    boolean existsByName(String name);
}
