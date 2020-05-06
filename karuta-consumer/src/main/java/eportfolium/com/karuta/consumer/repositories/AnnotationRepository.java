package eportfolium.com.karuta.consumer.repositories;

import eportfolium.com.karuta.model.bean.Annotation;
import eportfolium.com.karuta.model.bean.AnnotationId;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnnotationRepository extends CrudRepository<Annotation, AnnotationId> {
}
