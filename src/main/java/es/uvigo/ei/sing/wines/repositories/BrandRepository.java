package es.uvigo.ei.sing.wines.repositories;

import es.uvigo.ei.sing.wines.entities.BrandEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BrandRepository extends CrudRepository<BrandEntity, Integer> {
    Optional<BrandEntity> findByName(String brandName);
}
