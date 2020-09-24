package es.uvigo.ei.sing.wines.services;

import es.uvigo.ei.sing.wines.entities.HtmlEntity;
import es.uvigo.ei.sing.wines.repositories.HtmlRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class HtmlService {

    private final HtmlRepository htmlRepository;

    @Autowired
    public HtmlService(HtmlRepository htmlRepository) {
        this.htmlRepository = htmlRepository;
    }

    public HtmlEntity save(HtmlEntity htmlEntity) {
        return htmlRepository.save(htmlEntity);
    }

    public Iterable<HtmlEntity> findAll() {
        return htmlRepository.findAll();
    }

    public Set<HtmlEntity> findByHtmlIsNull() {
        return htmlRepository.findByHtmlIsNull();
    }
}
