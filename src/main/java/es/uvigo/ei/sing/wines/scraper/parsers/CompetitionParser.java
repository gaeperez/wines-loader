package es.uvigo.ei.sing.wines.scraper.parsers;

import es.uvigo.ei.sing.wines.entities.CompetitionEntity;
import es.uvigo.ei.sing.wines.entities.WebEntity;
import es.uvigo.ei.sing.wines.services.CompetitionService;
import lombok.extern.log4j.Log4j2;
import org.apache.tika.utils.ExceptionUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

@Log4j2
public class CompetitionParser {

    private final CompetitionService competitionService;

    public CompetitionParser(CompetitionService competitionService) {
        this.competitionService = competitionService;
    }

    public void parse(Page<WebEntity> webWineEntities) {
        String url = "", html;
        for (WebEntity webWineEntity : webWineEntities) {
            try {
                url = webWineEntity.getUrl();
                html = webWineEntity.getHtmlEntity().getHtml();
                log.info("Starting the parsing of the competitions contained in the following wine URL: {}...", url);

                // Parse the URL and get the body
                Element bodyElement = Jsoup.parse(html).body();

                // Get the section with information about the product and parse it
                Element productElement = bodyElement.getElementById("product_content");
                parseCompetitions(productElement);

                log.info("Finishing the parsing of competitions contained in the following wine URL: {}...", url);
            } catch (Exception e) {
                log.error("An error has occurred during the parsing of competitions for wine: {}. See error: {}", url,
                        ExceptionUtils.getFilteredStackTrace(e));
            }
        }
    }

    private void parseCompetitions(Element productElement) {
        // Get right information
        Element rightInfoElement = productElement.getElementById("sheet").selectFirst("table[class='attributes-box-right']");
        Element element;
        if (rightInfoElement != null) {
            // Get awards
            element = rightInfoElement.selectFirst("th[title='Awards']");
            if (element != null) {
                // Get all competitions from HTML
                List<TextNode> awardsNodes = element.nextElementSibling().textNodes();

                String awardName;
                CompetitionEntity competitionEntity;
                for (TextNode awardNode : awardsNodes) {
                    // Get competition name (unique key)
                    awardName = awardNode.text().trim();

                    // Check if the competition is already inserted
                    Optional<CompetitionEntity> possibleCompetitionEntity = competitionService.findByAward(awardName);
                    if (!possibleCompetitionEntity.isPresent()) {
                        competitionEntity = new CompetitionEntity();
                        competitionEntity.setAward(awardName);

                        // Save the competition in the database
                        log.info("Saving the following competition entity: {}, in the database...", awardName);
                        competitionService.save(competitionEntity);
                    }
                }
            }
        }
    }
}
