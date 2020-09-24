package es.uvigo.ei.sing.wines.repositories;

import es.uvigo.ei.sing.wines.entities.CompetitionEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompetitionRepository extends CrudRepository<CompetitionEntity, Integer> {
    Optional<CompetitionEntity> findByAward(String awardName);
}
