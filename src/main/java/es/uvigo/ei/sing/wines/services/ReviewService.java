package es.uvigo.ei.sing.wines.services;

import es.uvigo.ei.sing.wines.entities.ReviewEntity;
import es.uvigo.ei.sing.wines.repositories.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;

    @Autowired
    public ReviewService(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    public Optional<ReviewEntity> findByHash(String hash) {
        return reviewRepository.findByHash(hash);
    }
}
