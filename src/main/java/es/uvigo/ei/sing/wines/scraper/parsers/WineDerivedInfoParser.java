package es.uvigo.ei.sing.wines.scraper.parsers;

import es.uvigo.ei.sing.wines.entities.WebEntity;
import es.uvigo.ei.sing.wines.entities.WebEntityType;
import es.uvigo.ei.sing.wines.services.WebService;
import es.uvigo.ei.sing.wines.utils.AppFunctions;
import lombok.extern.log4j.Log4j2;
import org.apache.tika.utils.ExceptionUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;

@Log4j2
public class WineDerivedInfoParser {

    private final WebService webService;

    public WineDerivedInfoParser(WebService webService) {
        this.webService = webService;
    }

    public void parse(Page<WebEntity> webWineEntities) {
        String url = "", html;
        int numberOfReviews;
        for (WebEntity webWineEntity : webWineEntities) {
            try {
                url = webWineEntity.getUrl();
                html = webWineEntity.getHtmlEntity().getHtml();
                numberOfReviews = 0;
                log.info("Starting the parsing of the following derived information from the URL: {}...", url);

                // Parse the URL and get the body
                Element bodyElement = Jsoup.parse(html).body();

                // Locate and save the derived information of the wine
                Element producerElement = bodyElement.getElementById("product_content");
                parseDerivedTechnicalInformation(producerElement);

                // Get information about the user reviews (first ten in page)
                Element ratingCount = producerElement.selectFirst("strong[itemprop='ratingCount']");
                if (ratingCount != null)
                    numberOfReviews = Integer.parseInt(ratingCount.text());
                parseOpinionsInformation(numberOfReviews, url);

                log.info("Finishing the parsing of the following derived information from the URL: {}...", url);
            } catch (Exception e) {
                log.error("An error has occurred during the parsing of derived information: {}. See error: {}", url,
                        ExceptionUtils.getFilteredStackTrace(e));
            }
        }
    }

    private void parseDerivedTechnicalInformation(Element productElement) {
        Element technicalElement = productElement.getElementById("sheet");

        /// Get left information
        Element leftInfoElement = technicalElement.selectFirst("table[class='attributes-box-left']");
        Element element;
        if (leftInfoElement != null) {
            // Get appellation
            element = leftInfoElement.selectFirst("th[title='Appellation']");
            if (element != null) {
                // There are at least two hyperlinks to retrieve the appellation info (appellation and location)
                Elements appellationElements = element.nextElementSibling().select("a[href]");

                element = appellationElements.first();
                if (element != null)
                    createAndSaveWebEntity(element.attr("href"), WebEntityType.appellation);

                element = appellationElements.last();
                if (element != null)
                    createAndSaveWebEntity(element.attr("href"), WebEntityType.location);
            }
            // Get winery
            element = leftInfoElement.selectFirst("th[title='Producer']");
            if (element != null) {
                element = element.nextElementSibling().selectFirst("a[href]");
                if (element != null)
                    createAndSaveWebEntity(element.attr("href"), WebEntityType.brand);
            }
        }

        // Get right information
        Element rightInfoElement = technicalElement.selectFirst("table[class='attributes-box-right']");
        if (rightInfoElement != null) {
            // Get grape urls
            element = rightInfoElement.selectFirst("th[title='Grapes']");
            if (element != null) {
                Elements grapesUrl = element.nextElementSibling().select("a[href]");
                for (Element grapeUrl : grapesUrl)
                    createAndSaveWebEntity(grapeUrl.attr("href"), WebEntityType.grape);
            }
            // Get pairings
            element = rightInfoElement.selectFirst("th[title='Pairing']");
            if (element != null) {
                Elements foodsUrl = element.nextElementSibling().select("a[href]");
                for (Element foodUrl : foodsUrl)
                    createAndSaveWebEntity(foodUrl.attr("href"), WebEntityType.pairing);
            }
        }
    }

    private void parseOpinionsInformation(int numberOfReviews, String wineUrl) {
        // The first review does not appear
        if (numberOfReviews > 1) {
            // {1}: wine url, {2}: reviews index (10, 20, 30...)
            String reviewsUrl = "https://www.drinksandco.com/products-opinions/%s:f:%d";

            // Get the last part of the URL (ex. blanc-pescador)
            wineUrl = AppFunctions.getLastPartOfURL(wineUrl);

            for (int index = 1; index <= numberOfReviews; index += 10)
                createAndSaveWebEntity(String.format(reviewsUrl, wineUrl, index), WebEntityType.review);
        }
    }

    private void createAndSaveWebEntity(String url, WebEntityType webEntityType) {
        String hash = AppFunctions.doHash(url);

        if (!webService.findByHash(hash).isPresent()) {
            // Save it without the html (the page is not fetched yet)
            WebEntity webEntity = new WebEntity();
            webEntity.setUrl(url);
            webEntity.setModified(LocalDateTime.now());
            webEntity.setHash(hash);
            webEntity.setType(webEntityType);

            webService.save(webEntity);
        }
    }
}
