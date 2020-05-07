package eportfolium.com.karuta.consumer.repositories;

import eportfolium.com.karuta.model.bean.Configuration;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConfigurationRepository extends CrudRepository<Configuration, Long> {
    Optional<Configuration> findByName(String name);

    boolean existsByName(String name);
}
