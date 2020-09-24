package es.uvigo.ei.sing.wines.services;

import es.uvigo.ei.sing.wines.entities.CompetitionEntity;
import es.uvigo.ei.sing.wines.repositories.CompetitionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CompetitionService {

    private final CompetitionRepository competitionRepository;

    @Autowired
    public CompetitionService(CompetitionRepository competitionRepository) {
        this.competitionRepository = competitionRepository;
    }

    public CompetitionEntity save(CompetitionEntity competitionEntity) {
        return competitionRepository.save(competitionEntity);
    }

    public Optional<CompetitionEntity> findByAward(String awardName) {
        return competitionRepository.findByAward(awardName);
    }
}
