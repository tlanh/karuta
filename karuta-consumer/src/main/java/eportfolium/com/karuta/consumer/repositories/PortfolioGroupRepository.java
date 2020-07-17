package eportfolium.com.karuta.consumer.repositories;

import eportfolium.com.karuta.model.bean.PortfolioGroup;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PortfolioGroupRepository extends CrudRepository<PortfolioGroup, Long> {
    Optional<PortfolioGroup> findByLabel(String label);

    boolean existsByIdAndType(Long id, String type);
}
