package es.uvigo.ei.sing.wines.repositories;

import es.uvigo.ei.sing.wines.entities.HtmlEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface HtmlRepository extends CrudRepository<HtmlEntity, Integer> {
    Set<HtmlEntity> findByHtmlIsNull();
}
