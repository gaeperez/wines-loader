package es.uvigo.ei.sing.wines.services;

import es.uvigo.ei.sing.wines.entities.LocationEntity;
import es.uvigo.ei.sing.wines.repositories.LocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LocationService {

    private final LocationRepository locationRepository;

    @Autowired
    public LocationService(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    public LocationEntity save(LocationEntity locationEntity) {
        return locationRepository.save(locationEntity);
    }

    public Optional<LocationEntity> findByName(String locationName) {
        return locationRepository.findByName(locationName);
    }
}
