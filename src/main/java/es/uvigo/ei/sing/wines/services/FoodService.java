package es.uvigo.ei.sing.wines.services;

import es.uvigo.ei.sing.wines.entities.FoodEntity;
import es.uvigo.ei.sing.wines.repositories.FoodRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class FoodService {

    private final FoodRepository foodRepository;

    @Autowired
    public FoodService(FoodRepository foodRepository) {
        this.foodRepository = foodRepository;
    }

    public FoodEntity save(FoodEntity foodEntity) {
        return foodRepository.save(foodEntity);
    }

    public Optional<FoodEntity> findByName(String foodName) {
        return foodRepository.findByName(foodName);
    }
}
