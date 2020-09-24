package es.uvigo.ei.sing.wines.services;

import es.uvigo.ei.sing.wines.entities.WebEntity;
import es.uvigo.ei.sing.wines.entities.WebEntityType;
import es.uvigo.ei.sing.wines.repositories.WebRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

@Service
public class WebService {

    private final WebRepository webRepository;

    @Autowired
    public WebService(WebRepository webRepository) {
        this.webRepository = webRepository;
    }

    public WebEntity save(WebEntity webEntity) {
        return webRepository.save(webEntity);
    }

    public WebEntity saveOrGet(WebEntity webEntity) {
        // Check if the entity is already inserted
        Optional<WebEntity> possibleWebEntity = this.findByHash(webEntity.getHash());

        return possibleWebEntity.orElseGet(() -> webRepository.save(webEntity));
    }

    public Iterable<WebEntity> findAll() {
        return webRepository.findAll();
    }

    public Set<WebEntity> findAll(Pageable pageable) {
        return webRepository.findAll(pageable);
    }

    public Set<WebEntity> findByType(WebEntityType webEntityType) {
        return webRepository.findByType(webEntityType);
    }

    public Set<WebEntity> findByTypeAndHtmlEntityNull(WebEntityType webEntityType) {
        return webRepository.findByTypeAndHtmlEntityNull(webEntityType);
    }

    public Page<WebEntity> findByTypeAndHtmlEntityNull(WebEntityType webEntityType, Pageable page) {
        return webRepository.findByTypeAndHtmlEntityNull(webEntityType, page);
    }

    public Set<WebEntity> findByTypeAndHtmlEntityIsNotNull(WebEntityType webEntityType) {
        return webRepository.findByTypeAndHtmlEntityIsNotNull(webEntityType);
    }

    public Page<WebEntity> findByTypeAndHtmlEntityIsNotNull(WebEntityType webEntityType, Pageable pageable) {
        return webRepository.findByTypeAndHtmlEntityIsNotNull(webEntityType, pageable);
    }

    public Optional<WebEntity> findByHash(String hash) {
        return webRepository.findByHash(hash);
    }

    public Page<WebEntity> findByTypeAndUrlEndsWith(WebEntityType type, String url, Pageable pageable) {
        return webRepository.findByTypeAndUrlEndsWith(type, url, pageable);
    }
}
