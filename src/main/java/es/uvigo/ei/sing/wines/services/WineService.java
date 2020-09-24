package es.uvigo.ei.sing.wines.services;

import es.uvigo.ei.sing.wines.entities.WineEntity;
import es.uvigo.ei.sing.wines.repositories.WineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class WineService {

    private final WineRepository wineRepository;

    @Autowired
    public WineService(WineRepository wineRepository) {
        this.wineRepository = wineRepository;
    }

    public WineEntity save(WineEntity wineEntity) {
        return wineRepository.save(wineEntity);
    }

    public Optional<WineEntity> findByHash(String hash) {
        return wineRepository.findByHash(hash);
    }
}
