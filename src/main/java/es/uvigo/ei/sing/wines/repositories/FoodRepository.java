package es.uvigo.ei.sing.wines.repositories;

import es.uvigo.ei.sing.wines.entities.FoodEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FoodRepository extends CrudRepository<FoodEntity, Integer> {
    Optional<FoodEntity> findByName(String foodName);
}
