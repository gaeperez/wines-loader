package es.uvigo.ei.sing.wines.repositories;

import es.uvigo.ei.sing.wines.entities.AppellationEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AppellationRepository extends CrudRepository<AppellationEntity, Integer> {
    Optional<AppellationEntity> findByName(String appellationName);
}
