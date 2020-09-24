package es.uvigo.ei.sing.wines.repositories;

import es.uvigo.ei.sing.wines.entities.ReviewEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewRepository extends CrudRepository<ReviewEntity, Integer> {
    Optional<ReviewEntity> findByHash(String hash);
}
