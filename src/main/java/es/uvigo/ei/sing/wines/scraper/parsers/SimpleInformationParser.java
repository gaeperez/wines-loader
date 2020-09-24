package es.uvigo.ei.sing.wines.scraper.parsers;

import es.uvigo.ei.sing.wines.entities.*;
import es.uvigo.ei.sing.wines.services.AppellationService;
import es.uvigo.ei.sing.wines.services.FoodService;
import es.uvigo.ei.sing.wines.services.GrapeService;
import es.uvigo.ei.sing.wines.services.LocationService;
import es.uvigo.ei.sing.wines.utils.AppFunctions;
import lombok.extern.log4j.Log4j2;
import org.apache.tika.utils.ExceptionUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.springframework.data.domain.Page;

import java.util.Optional;

@Log4j2
public class SimpleInformationParser {

    private final GrapeService grapeService;
    private final AppellationService appellationService;
    private final LocationService locationService;
    private final FoodService foodService;

    public SimpleInformationParser(GrapeService grapeService, AppellationService appellationService,
                                   LocationService locationService, FoodService foodService) {
        this.grapeService = grapeService;
        this.appellationService = appellationService;
        this.locationService = locationService;
        this.foodService = foodService;
    }

    public void parse(Page<WebEntity> webEntities, WebEntityType webEntityType) {
        // WebEntity variables
        String url = "", html;
        // Current parsing entity variables
        String primaryKey;
        for (WebEntity webEntity : webEntities) {
            try {
                // Get the URL and the HTML in the WebEntity
                url = webEntity.getUrl();
                html = webEntity.getHtmlEntity().getHtml();
                log.info("Starting the parsing of the following {} URL: {}...", webEntityType.toString(), url);

                // Get the last part of grape URL to use as a key (ex. macabeo)
                primaryKey = AppFunctions.getLastPartOfURL(url);

                if (webEntityType == WebEntityType.grape)
                    parseGrapes(primaryKey, html);
                else if (webEntityType == WebEntityType.appellation)
                    parseAppellation(primaryKey, html);
                else if (webEntityType == WebEntityType.location)
                    parseLocation(primaryKey, html);
                else if (webEntityType == WebEntityType.pairing)
                    parsePairing(primaryKey, html);

                log.info("Finishing the parsing of the following {} URL: {}...", webEntityType.toString(), url);
            } catch (Exception e) {
                log.error("An error has occurred during the parsing of {}: {}. See error: {}", webEntityType.toString(), url,
                        ExceptionUtils.getFilteredStackTrace(e));
            }
        }
    }

    private void parseGrapes(String primaryKey, String html) {
        // Check if the grape is already inserted
        Optional<GrapeEntity> possibleGrapeEntity = grapeService.findByName(primaryKey);
        if (!possibleGrapeEntity.isPresent()) {
            // Parse the URL and get the body
            Element bodyElement = Jsoup.parse(html).body();

            // Create the grape entity
            GrapeEntity grapeEntity = new GrapeEntity();
            grapeEntity.setName(primaryKey);

            // Parse the description
            Element descriptionElement = bodyElement.selectFirst("div[id='page_description_content']");
            if (descriptionElement != null)
                grapeEntity.setDescription(descriptionElement.text().trim());

            // Save the grape in the database
            log.info("Saving the following grape entity: {}, in the database...", primaryKey);
            grapeService.save(grapeEntity);
        }
    }

    private void parseAppellation(String primaryKey, String html) {
        // Check if the appellation is already inserted
        Optional<AppellationEntity> possibleAppellationEntity = appellationService.findByName(primaryKey);
        if (!possibleAppellationEntity.isPresent()) {
            // Parse the URL and get the body
            Element bodyElement = Jsoup.parse(html).body();

            // Create the appellation entity
            AppellationEntity appellationEntity = new AppellationEntity();
            appellationEntity.setName(primaryKey);

            // Parse the description
            Element descriptionElement = bodyElement.selectFirst("div[id='page_description_content']");
            if (descriptionElement != null)
                appellationEntity.setDescription(descriptionElement.text().trim());

            // Save the appellation in the database
            log.info("Saving the following appellation entity: {}, in the database...", primaryKey);
            appellationService.save(appellationEntity);
        }
    }

    private void parseLocation(String primaryKey, String html) {
        // Check if the location is already inserted
        Optional<LocationEntity> possibleLocationEntity = locationService.findByName(primaryKey);
        if (!possibleLocationEntity.isPresent()) {
            // Parse the URL and get the body
            Element bodyElement = Jsoup.parse(html).body();

            // Create the location entity
            LocationEntity locationEntity = new LocationEntity();
            locationEntity.setName(primaryKey);

            // Parse the description
            Element descriptionElement = bodyElement.selectFirst("div[id='page_description_content']");
            if (descriptionElement != null)
                locationEntity.setDescription(descriptionElement.text().trim());

            // Save the location in the database
            log.info("Saving the following location entity: {}, in the database...", primaryKey);
            locationService.save(locationEntity);
        }
    }

    private void parsePairing(String primaryKey, String html) {
        // Check if the location is already inserted
        Optional<FoodEntity> possibleFoodEntity = foodService.findByName(primaryKey);
        if (!possibleFoodEntity.isPresent()) {
            // Parse the URL and get the body
            Element bodyElement = Jsoup.parse(html).body();

            // Create the food entity
            FoodEntity foodEntity = new FoodEntity();
            foodEntity.setName(primaryKey);

            // Parse the description
            Element descriptionElement = bodyElement.selectFirst("div[id='page_description_content']");
            if (descriptionElement != null)
                foodEntity.setDescription(descriptionElement.text().trim());

            // Save the food in the database
            log.info("Saving the following food entity: {}, in the database...", primaryKey);
            foodService.save(foodEntity);
        }
    }
}
