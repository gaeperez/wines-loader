package es.uvigo.ei.sing.wines.repositories;

import es.uvigo.ei.sing.wines.entities.WebEntity;
import es.uvigo.ei.sing.wines.entities.WebEntityType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface WebRepository extends CrudRepository<WebEntity, Integer> {
    Set<WebEntity> findAll(Pageable page);

    Optional<WebEntity> findByHash(String hash);

    Set<WebEntity> findByType(WebEntityType webEntityType);

    Set<WebEntity> findByTypeAndHtmlEntityNull(WebEntityType webEntityType);

    Page<WebEntity> findByTypeAndHtmlEntityNull(WebEntityType webEntityType, Pageable pageable);

    Set<WebEntity> findByTypeAndHtmlEntityIsNotNull(WebEntityType webEntityType);

    Page<WebEntity> findByTypeAndHtmlEntityIsNotNull(WebEntityType webEntityType, Pageable pageable);

    Page<WebEntity> findByTypeAndUrlEndsWith(WebEntityType type, String url, Pageable pageable);
}
