package es.uvigo.ei.sing.wines.repositories;

import es.uvigo.ei.sing.wines.entities.GrapeEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GrapeRepository extends CrudRepository<GrapeEntity, Integer> {
    Optional<GrapeEntity> findByName(String grapeName);
}
