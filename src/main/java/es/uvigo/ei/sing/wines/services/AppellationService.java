package es.uvigo.ei.sing.wines.services;

import es.uvigo.ei.sing.wines.entities.AppellationEntity;
import es.uvigo.ei.sing.wines.repositories.AppellationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AppellationService {

    private final AppellationRepository appellationRepository;

    @Autowired
    public AppellationService(AppellationRepository appellationRepository) {
        this.appellationRepository = appellationRepository;
    }

    public AppellationEntity save(AppellationEntity appellationEntity) {
        return appellationRepository.save(appellationEntity);
    }

    public Optional<AppellationEntity> findByName(String appellationName) {
        return appellationRepository.findByName(appellationName);
    }
}
