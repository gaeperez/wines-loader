package es.uvigo.ei.sing.wines.services;

import es.uvigo.ei.sing.wines.entities.GrapeEntity;
import es.uvigo.ei.sing.wines.repositories.GrapeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class GrapeService {

    private final GrapeRepository grapeRepository;

    @Autowired
    public GrapeService(GrapeRepository grapeRepository) {
        this.grapeRepository = grapeRepository;
    }

    public GrapeEntity save(GrapeEntity grapeEntity) {
        return grapeRepository.save(grapeEntity);
    }

    public Optional<GrapeEntity> findByName(String grapeName) {
        return grapeRepository.findByName(grapeName);
    }
}
