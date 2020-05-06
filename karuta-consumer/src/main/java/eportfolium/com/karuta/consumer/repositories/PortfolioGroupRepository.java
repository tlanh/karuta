package eportfolium.com.karuta.consumer.repositories;

import eportfolium.com.karuta.model.bean.PortfolioGroup;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface PortfolioGroupRepository extends CrudRepository<PortfolioGroup, Long> {
    Optional<PortfolioGroup> findByLabel(String label);

    boolean existsByIdAndType(Long id, String type);
}
