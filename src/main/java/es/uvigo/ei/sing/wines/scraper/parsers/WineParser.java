package es.uvigo.ei.sing.wines.scraper.parsers;

import es.uvigo.ei.sing.wines.entities.*;
import es.uvigo.ei.sing.wines.services.*;
import es.uvigo.ei.sing.wines.utils.AppFunctions;
import lombok.extern.log4j.Log4j2;
import org.apache.tika.utils.ExceptionUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.springframework.data.domain.Page;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Log4j2
public class WineParser {

    private final BrandService brandService;
    private final AppellationService appellationService;
    private final LocationService locationService;
    private final FoodService foodService;
    private final ReviewService reviewService;
    private final GrapeService grapeService;
    private final CompetitionService competitionService;
    private final WineService wineService;

    public WineParser(BrandService brandService, AppellationService appellationService,
                      LocationService locationService, FoodService foodService, ReviewService reviewService,
                      GrapeService grapeService, CompetitionService competitionService, WineService wineService) {
        this.brandService = brandService;
        this.appellationService = appellationService;
        this.locationService = locationService;
        this.foodService = foodService;
        this.reviewService = reviewService;
        this.grapeService = grapeService;
        this.competitionService = competitionService;
        this.wineService = wineService;
    }

    public void parse(Page<WebEntity> webWineEntities) {
        String url = "", hash, html;
        int numberOfReviews = 0;
        for (WebEntity webWineEntity : webWineEntities) {
            try {
                url = webWineEntity.getUrl();
                hash = webWineEntity.getHash();
                html = webWineEntity.getHtmlEntity().getHtml();

                // Check if the wine is already inserted
                Optional<WineEntity> possibleWineEntity = wineService.findByHash(hash);
                if (!possibleWineEntity.isPresent()) {
                    log.info("Starting the parsing of the following wine URL: {}...", url);

                    // Create entity and set basic information
                    WineEntity wineEntity = new WineEntity();
                    wineEntity.setSource(url);
                    wineEntity.setHash(hash);

                    // Parse the URL and get the body
                    Element bodyElement = Jsoup.parse(html).body();

                    // Get the section with information about the product and parse it
                    Element productElement = bodyElement.getElementById("product_content");
                    parseBasicProductInformation(productElement, wineEntity);
                    parseTechnicalProductInformation(productElement, wineEntity);

                    // Get information about the producer/winery/brand
                    Element producerElement = bodyElement.getElementById("productor");
                    parseProducerInformation(producerElement, wineEntity);

                    // Get information about the user reviews (first ten in page)
                    Element ratingCount = productElement.selectFirst("strong[itemprop='ratingCount']");
                    if (ratingCount != null)
                        numberOfReviews = Integer.parseInt(ratingCount.text());
                    parseOpinionsInformation(numberOfReviews, url, wineEntity);

                    // Save the wine in the database
                    log.info("Saving the following wine entity: {}, in the database...", url);
                    wineService.save(wineEntity);

                    log.info("Finishing the parsing of the following wine URL: {}...", url);
                } else
                    log.info("The wine with the URL: {}, is already inserted, skipping...", url);
            } catch (Exception e) {
                log.error("An error has occurred during the parsing of wine: {}. See error: {}", url,
                        ExceptionUtils.getFilteredStackTrace(e));
            }
        }
    }

    private void parseBasicProductInformation(Element productElement, WineEntity wineEntity) {
        // Get the information contained in the product title (there is only one element by this class)
        Element titleElement = productElement.getElementsByClass("product-title").first();
        wineEntity.setName(titleElement.selectFirst("strong[itemprop='name']").text());

        // Get the expert wine ratings
        Element ratingsElement = titleElement.selectFirst("p[class='ratings']");
        if (ratingsElement != null) {
            Element ratingElement;
            try {
                ratingElement = ratingsElement.selectFirst("span[class*='guia_pk']");
                if (ratingElement != null)
                    wineEntity.setRpRating(Float.parseFloat(AppFunctions.parseDecimalNumber(ratingElement.childNode(1).toString())));
            } catch (NumberFormatException e) {
                log.warn("Cannot transform into float the RP rating...");
            }
            try {
                ratingElement = ratingsElement.selectFirst("span[class*='guia_ws']");
                if (ratingElement != null)
                    wineEntity.setWsRating(Float.parseFloat(AppFunctions.parseDecimalNumber(ratingElement.childNode(1).toString())));
            } catch (NumberFormatException e) {
                log.warn("Cannot transform into float the WS rating...");
            }
            try {
                ratingElement = ratingsElement.selectFirst("span[class*='guia_wine_enthusiast']");
                if (ratingElement != null)
                    wineEntity.setWeRating(Float.parseFloat(AppFunctions.parseDecimalNumber(ratingElement.childNode(1).toString())));
            } catch (NumberFormatException e) {
                log.warn("Cannot transform into float the WE rating...");
            }
        }

        // Get the information about the price
        Element price = productElement.selectFirst("span[class='price'][itemprop='price']");
        if (price != null)
            wineEntity.setPrice(Float.parseFloat(price.attr("content")));
        wineEntity.setExternalId(productElement.selectFirst("div[data-id-product]").attr("data-id-product"));

        // Get the information about the uvinum rating
        Element ratingElement = productElement.getElementById("rating_box").selectFirst("strong[itemprop='ratingValue']");
        if (ratingElement != null)
            wineEntity.setDcRating(Float.parseFloat(AppFunctions.parseDecimalNumber(ratingElement.text())));

        // Get the information about the summary
        Element summaryElement = productElement.getElementsByClass("review-body").first();
        if (summaryElement != null)
            wineEntity.setSummary(summaryElement.text());

        // Get the information about the content description
        Element contentElement = productElement.getElementsByClass("content-description").first();
        if (contentElement != null)
            wineEntity.setDescription(contentElement.selectFirst("div[class='review-description']").text());
    }

    private void parseTechnicalProductInformation(Element productElement, WineEntity wineEntity) {
        Element technicalElement = productElement.getElementById("sheet");

        // Get left side information
        parseLeftInformation(wineEntity, technicalElement);

        // Get right side information
        parseRightInformation(wineEntity, technicalElement);

        // Get other information
        parseOtherInformation(wineEntity, technicalElement);
    }

    private void parseLeftInformation(WineEntity wineEntity, Element technicalElement) {
        Element element;
        String url;

        Element leftInfoElement = technicalElement.selectFirst("table[class='attributes-box-left']");
        if (leftInfoElement != null) {
            // Get wine type
            element = leftInfoElement.selectFirst("th[title='Type of wine']");
            if (element != null)
                wineEntity.setType(element.nextElementSibling().text());
            // Get vintage number
            element = leftInfoElement.selectFirst("td[class*='anadas']");
            if (element != null) {
                element = element.selectFirst("strong");
                if (element != null)
                    wineEntity.setVintage(Integer.parseInt(element.text().replaceAll("[^\\d]", "")));
            }
            // Get appellation
            element = leftInfoElement.selectFirst("th[title='Appellation']");
            if (element != null) {
                // There are at least two hyperlinks to retrieve the appellation info (appellation and location)
                Elements appellationElements = element.nextElementSibling().select("a[href]");

                element = appellationElements.first();
                if (element != null) {
                    // Get the last part of appellation URL to use as a key (ex. emporda-wine)
                    url = AppFunctions.getLastPartOfURL(element.attr("href"));
                    Optional<AppellationEntity> possibleAppellation = appellationService.findByName(url);
                    if (possibleAppellation.isPresent())
                        wineEntity.setAppellation(possibleAppellation.get());
                    else {
                        Optional<LocationEntity> possibleLocation = locationService.findByName(url);
                        possibleLocation.ifPresent(wineEntity::setLocation);
                    }
                }

                element = appellationElements.last();
                if (element != null) {
                    // Get the last part of location URL to use as a key (ex. region-catalonia-wine)
                    url = AppFunctions.getLastPartOfURL(element.attr("href"));
                    Optional<LocationEntity> possibleLocation = locationService.findByName(url);
                    possibleLocation.ifPresent(wineEntity::setLocation);
                }
            }
            // Get winery
            element = leftInfoElement.selectFirst("th[title='Producer']");
            if (element != null) {
                element = element.nextElementSibling().selectFirst("a[href]");
                if (element != null) {
                    // Get the title of the hyperlink to use as a key (ex. Castillo de Perelada)
                    url = element.attr("title");
                    Optional<BrandEntity> possibleBrand = brandService.findByName(url);
                    possibleBrand.ifPresent(wineEntity::setBrand);
                }
            }
            // Get volume
            element = leftInfoElement.selectFirst("th[title='Volume']");
            if (element != null)
                wineEntity.setVolume(element.nextElementSibling().selectFirst("strong").ownText());
        }
    }

    private void parseRightInformation(WineEntity wineEntity, Element technicalElement) {
        Element element;
        String url;

        Element rightInfoElement = technicalElement.selectFirst("table[class='attributes-box-right']");
        if (rightInfoElement != null) {
            // Get grape urls
            element = rightInfoElement.selectFirst("th[title='Grapes']");
            if (element != null) {
                Elements grapesUrl = element.nextElementSibling().select("a[href]");
                Set<GrapeEntity> grapeEntities = new HashSet<>();
                // Avoid inserting two times the same grape
                Set<String> grapeKeys = new HashSet<>();
                for (Element grapeUrl : grapesUrl) {
                    // Get the last part of grape URL to use as a key (ex. macabeo)
                    url = AppFunctions.getLastPartOfURL(grapeUrl.attr("href"));
                    Optional<GrapeEntity> possibleGrape = grapeService.findByName(url);
                    if (possibleGrape.isPresent() && !grapeKeys.contains(url)) {
                        grapeEntities.add(possibleGrape.get());
                        grapeKeys.add(url);
                    }
                }
                wineEntity.setGrapes(grapeEntities);
            }
            // Get pairings
            element = rightInfoElement.selectFirst("th[title='Pairing']");
            if (element != null) {
                Elements foodsUrl = element.nextElementSibling().select("a[href]");
                Set<FoodEntity> foodEntities = new HashSet<>();
                // Avoid inserting the same food two times
                Set<String> foodKeys = new HashSet<>();
                for (Element foodUrl : foodsUrl) {
                    // Get the last part of grape URL to use as a key (ex. macabeo)
                    url = AppFunctions.getLastPartOfURL(foodUrl.attr("href"));
                    Optional<FoodEntity> possibleFood = foodService.findByName(url);
                    if (possibleFood.isPresent() && !foodKeys.contains(url)) {
                        foodEntities.add(possibleFood.get());
                        foodKeys.add(url);
                    }
                }
                wineEntity.setFoods(foodEntities);
            }
            // Get allergens
            element = rightInfoElement.selectFirst("th[title='Allergens']");
            if (element != null)
                wineEntity.setAllergens(element.nextElementSibling().text());
            // Get alcohol volume
            element = rightInfoElement.selectFirst("th[title*='Alcohol vol.']");
            if (element != null) {
                try {
                    // Replace everything except numbers
                    wineEntity.setAlcoholVol(Float.parseFloat(AppFunctions.parseDecimalNumber(element.nextElementSibling().text())));
                } catch (NumberFormatException e) {
                    log.warn("Cannot transform into float the alcohol volume...");
                }
            }
            // Get awards
            element = rightInfoElement.selectFirst("th[title='Awards']");
            if (element != null) {
                // Get all competitions from HTML
                List<TextNode> awardsNodes = element.nextElementSibling().textNodes();

                Set<CompetitionEntity> competitionEntities = new HashSet<>();
                // Avoid inserting two times the same competition
                Set<String> competitionKeys = new HashSet<>();
                String awardName;
                for (TextNode awardNode : awardsNodes) {
                    // Get competition name (unique key)
                    awardName = awardNode.text().trim();
                    Optional<CompetitionEntity> possibleCompetitionEntity = competitionService.findByAward(awardName);
                    if (possibleCompetitionEntity.isPresent() && !competitionKeys.contains(awardName)) {
                        competitionEntities.add(possibleCompetitionEntity.get());
                        competitionKeys.add(awardName);
                    }
                }
                wineEntity.setCompetitions(competitionEntities);
            }
        }
    }

    private void parseOtherInformation(WineEntity wineEntity, Element technicalElement) {
        Element element;

        Element otherInfoElement = technicalElement.selectFirst("table[class='attributes-box-others']");
        if (otherInfoElement != null) {
            // Get consumption temperatures
            element = otherInfoElement.selectFirst("th[title='Consumption temp.']");
            if (element != null) {
                String[] temperatures = element.nextElementSibling().text().replaceAll("[^\\d.,\\s]", " ")
                        .replaceAll("\\s+", " ").replaceAll(",", ".").trim().split(" ");
                if (temperatures.length == 1) {
                    wineEntity.setMinConsumptionTemp(Math.round(Float.parseFloat(temperatures[0])));
                    wineEntity.setMaxConsumptionTemp(Math.round(Float.parseFloat(temperatures[0])));
                } else if (temperatures.length > 1) {
                    wineEntity.setMinConsumptionTemp(Math.round(Float.parseFloat(temperatures[0])));
                    wineEntity.setMaxConsumptionTemp(Math.round(Float.parseFloat(temperatures[1])));
                }
            }
            // Get type of sparkling
            element = otherInfoElement.selectFirst("th[title='Type of Sparkling']");
            if (element != null)
                wineEntity.setType(wineEntity.getType() + " (" + element.nextElementSibling().text() + ")");
            // Get aging
            element = otherInfoElement.selectFirst("th[title='Aging']");
            if (element != null)
                wineEntity.setAging(element.nextElementSibling().text());
        }
    }

    private void parseProducerInformation(Element producerElement, WineEntity wineEntity) {
        if (producerElement != null) {
            Element element = producerElement.selectFirst("div[itemprop='description']");
            if (element != null) {
                String text = element.text().trim();
                int maxLength = Math.min(text.length(), 3000);

                // Limit the length of the text
                wineEntity.setTastingNotes(text.substring(0, maxLength));
            }
        }
    }

    private void parseOpinionsInformation(int numberOfReviews, String wineUrl, WineEntity wineEntity) {
        Set<ReviewEntity> reviewEntities = new HashSet<>();
        if (numberOfReviews > 1) {
            // {1}: wine url, {2}: review page (10, 20, 30...), {3}: review index (1, 2, 3...)
            String reviewsUrl = "https://www.drinksandco.com/products-opinions/%s:f:%d:%d";

            // Get the last part of the URL (ex. blanc-pescador)
            wineUrl = AppFunctions.getLastPartOfURL(wineUrl);

            String formattedUrl;
            int reviewNumber;
            for (int index = 1; index <= numberOfReviews; index += 10) {
                reviewNumber = index;
                for (int count = 1; count < 11; count++) {
                    // Break the loop because there are no more reviews
                    if (reviewNumber > numberOfReviews)
                        break;

                    // Example url for reviews :f:1:1, :f:11:1 ... :f:21:1, :f:21:2
                    formattedUrl = String.format(reviewsUrl, wineUrl, index, count);

                    // Find the review in the database
                    Optional<ReviewEntity> possibleReview = reviewService.findByHash(AppFunctions.doHash(formattedUrl));
                    possibleReview.ifPresent(reviewEntities::add);

                    reviewNumber++;
                }
            }
        }
        wineEntity.setReviews(reviewEntities);
    }
}
