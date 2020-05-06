package eportfolium.com.karuta.consumer.repositories;

import eportfolium.com.karuta.model.bean.LogTable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LogTableRepository extends CrudRepository<LogTable, Integer> {
}
