package eportfolium.com.karuta.consumer.repositories;

import eportfolium.com.karuta.model.bean.DataTable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DataTableRepository extends CrudRepository<DataTable, String> {
}
