package es.uvigo.ei.sing.wines.repositories;

import es.uvigo.ei.sing.wines.entities.WineEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WineRepository extends CrudRepository<WineEntity, Integer> {
    Optional<WineEntity> findByHash(String hash);
}
