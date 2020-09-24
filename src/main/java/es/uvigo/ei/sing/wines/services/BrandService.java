package es.uvigo.ei.sing.wines.services;

import es.uvigo.ei.sing.wines.entities.BrandEntity;
import es.uvigo.ei.sing.wines.repositories.BrandRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BrandService {

    private final BrandRepository brandRepository;

    @Autowired
    public BrandService(BrandRepository brandRepository) {
        this.brandRepository = brandRepository;
    }

    public BrandEntity save(BrandEntity brandEntity) {
        return brandRepository.save(brandEntity);
    }

    public Optional<BrandEntity> findByName(String brandName) {
        return brandRepository.findByName(brandName);
    }
}
