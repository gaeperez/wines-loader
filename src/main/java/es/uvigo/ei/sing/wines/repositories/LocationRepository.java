package es.uvigo.ei.sing.wines.repositories;

import es.uvigo.ei.sing.wines.entities.LocationEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LocationRepository extends CrudRepository<LocationEntity, Integer> {
    Optional<LocationEntity> findByName(String locationName);
}
