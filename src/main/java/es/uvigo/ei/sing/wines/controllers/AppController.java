package es.uvigo.ei.sing.wines.controllers;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import es.uvigo.ei.sing.wines.entities.WebEntity;
import es.uvigo.ei.sing.wines.entities.WebEntityType;
import es.uvigo.ei.sing.wines.scraper.crawlers.ListCrawler;
import es.uvigo.ei.sing.wines.scraper.crawlers.WineCrawler;
import es.uvigo.ei.sing.wines.scraper.parsers.*;
import es.uvigo.ei.sing.wines.services.*;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;

import java.util.Set;
import java.util.stream.IntStream;

@Log4j2
@Controller
public class AppController {

    private final BrandService brandService;
    private final AppellationService appellationService;
    private final LocationService locationService;
    private final FoodService foodService;
    private final ReviewService reviewService;
    private final UserService userService;
    private final GrapeService grapeService;
    private final WebService webService;
    private final HtmlService htmlService;
    private final CompetitionService competitionService;
    private final WineService wineService;

    @Autowired
    public AppController(BrandService brandService, AppellationService appellationService,
                         LocationService locationService, FoodService foodService, ReviewService reviewService,
                         UserService userService, GrapeService grapeService, WebService webService, HtmlService htmlService,
                         CompetitionService competitionService, WineService wineService) {
        this.brandService = brandService;
        this.appellationService = appellationService;
        this.locationService = locationService;
        this.foodService = foodService;
        this.reviewService = reviewService;
        this.userService = userService;
        this.grapeService = grapeService;
        this.webService = webService;
        this.htmlService = htmlService;
        this.competitionService = competitionService;
        this.wineService = wineService;
    }

    public void crawlListOfProducts() {
        try {
            // Crawler variables
            String url = "https://www.drinksandco.com/wines:v:all";
            int maxDepth = -1;
            int politenessDelay = 5000;
            int numCrawlers = 1;
            boolean isResumable = true;
            String storageFolder = "crawler/lists";

            // Configure the crawler
            CrawlController crawlController = prepareCrawler(maxDepth, politenessDelay, isResumable, storageFolder);

            // Add seed URL to be crawled
            crawlController.addSeed(url);

            // Create the crawler
            CrawlController.WebCrawlerFactory<ListCrawler> factory = () -> new ListCrawler(webService, htmlService, false);

            // Start the crawler. This is a blocking operation for this thread
            log.info("Starting the crawling of the product URLs from the base URL {}...", url);
            crawlController.start(factory, numCrawlers);
            log.info("Finishing the crawling of the product URLs...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void crawlListUntilLastPage() {
        try {
            int maxDepth = 0;
            int politenessDelay = 5000;
            int numCrawlers = 1;
            boolean isResumable = false;
            String storageFolder = "crawler/lists_last";

            Pageable pageable = PageRequest.of(0, 1);
            Page<WebEntity> allListsWithPageTen = null;
            // Get until there is no more entities
            while (allListsWithPageTen == null || allListsWithPageTen.hasNext()) {
                // Get the tenth page of all lists
                allListsWithPageTen = webService.findByTypeAndUrlEndsWith(WebEntityType.list, ":10", pageable);

                // Configure the crawler
                CrawlController crawlController = prepareCrawler(maxDepth, politenessDelay, isResumable, storageFolder);
                // It is necessary to not respect the noFollow values to crawl these URLs
                crawlController.getConfig().setRespectNoFollow(false);
                crawlController.getConfig().setRespectNoIndex(false);

                // Add seed URL (i.e. changes :10 for :11, :12... :60)
                allListsWithPageTen.stream().forEach(webEntity -> IntStream.rangeClosed(11, 60).forEach(index ->
                        crawlController.addSeed(webEntity.getUrl().replaceFirst("10$", String.valueOf(index)))));

                // Create the crawler
                CrawlController.WebCrawlerFactory<ListCrawler> factory = () -> new ListCrawler(webService, htmlService, true);

                // Start the crawler. This is a blocking operation for this thread
                log.info("Starting the crawling of the product URLs from the page 11 to the page 60. Current page {} of {}...", allListsWithPageTen.getNumber(), allListsWithPageTen.getTotalPages());
                crawlController.start(factory, numCrawlers);
                log.info("Finishing the crawling of the product URLs from the page 11 to the page 60 for the page {}...", allListsWithPageTen.getNumber());

                // Go to the next page
                pageable = pageable.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void crawlProducts() {
        try {
            int maxDepth = 0;
            int politenessDelay = 5000;
            int numCrawlers = 1;
            boolean isResumable = false;
            String storageFolder = "crawler/products";

            Pageable pageable = PageRequest.of(0, 1000);
            Page<WebEntity> unparsedWebEntities = null;
            // Get until there is no more entities
            while (unparsedWebEntities == null || unparsedWebEntities.hasNext()) {
                // Get from Web table the entries without HTML (not parsed products)
                unparsedWebEntities = webService.findByTypeAndHtmlEntityNull(WebEntityType.wine, pageable);

                // Configure the crawler
                CrawlController crawlController = prepareCrawler(maxDepth, politenessDelay, isResumable, storageFolder);

                // Add the URLs to be crawled
                unparsedWebEntities.stream().forEach(web -> crawlController.addSeed(web.getUrl()));

                // Create the crawler
                CrawlController.WebCrawlerFactory<WineCrawler> factory = () -> new WineCrawler(webService);

                // Start the crawler. This is a blocking operation for this thread
                log.info("Starting the retrieval of the HTML of the products. {} pages left...", unparsedWebEntities.getTotalPages());
                crawlController.start(factory, numCrawlers);
                log.info("Finishing the retrieval of the HTML of the products for the page {}...", unparsedWebEntities.getTotalPages());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void crawlBrands() {
        try {
            // Get from Web table the entries without HTML (not parsed products)
            Set<WebEntity> unparsedWebEntities = webService.findByTypeAndHtmlEntityNull(WebEntityType.brand);
            int maxDepth = 0;
            int politenessDelay = 5000;
            int numCrawlers = 1;
            boolean isResumable = false;
            String storageFolder = "crawler/brands";

            // Configure the crawler
            CrawlController crawlController = prepareCrawler(maxDepth, politenessDelay, isResumable, storageFolder);

            // Add the URLs to be crawled
            unparsedWebEntities.forEach(web -> crawlController.addSeed(web.getUrl()));

            // Create the crawler
            CrawlController.WebCrawlerFactory<WineCrawler> factory = () -> new WineCrawler(webService);

            // Start the crawler. This is a blocking operation for this thread
            log.info("Starting the retrieval of the HTML of the brands...");
            crawlController.start(factory, numCrawlers);
            log.info("Finishing the retrieval of the HTML of the brands...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void crawlGrapes() {
        try {
            // Get from Web table the entries without HTML (not parsed products)
            Set<WebEntity> unparsedWebEntities = webService.findByTypeAndHtmlEntityNull(WebEntityType.grape);
            int maxDepth = 0;
            int politenessDelay = 5000;
            int numCrawlers = 1;
            boolean isResumable = false;
            String storageFolder = "crawler/grapes";

            // Configure the crawler
            CrawlController crawlController = prepareCrawler(maxDepth, politenessDelay, isResumable, storageFolder);

            // Add the URLs to be crawled
            unparsedWebEntities.forEach(web -> crawlController.addSeed(web.getUrl()));

            // Create the crawler
            CrawlController.WebCrawlerFactory<WineCrawler> factory = () -> new WineCrawler(webService);

            // Start the crawler. This is a blocking operation for this thread
            log.info("Starting the retrieval of the HTML of the grapes...");
            crawlController.start(factory, numCrawlers);
            log.info("Finishing the retrieval of the HTML of the grapes...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void crawlAppellations() {
        try {
            // Get from Web table the entries without HTML (not parsed products)
            Set<WebEntity> unparsedWebEntities = webService.findByTypeAndHtmlEntityNull(WebEntityType.appellation);
            int maxDepth = 0;
            int politenessDelay = 5000;
            int numCrawlers = 1;
            boolean isResumable = false;
            String storageFolder = "crawler/appellations";

            // Configure the crawler
            CrawlController crawlController = prepareCrawler(maxDepth, politenessDelay, isResumable, storageFolder);

            // Add the URLs to be crawled
            unparsedWebEntities.forEach(web -> crawlController.addSeed(web.getUrl()));

            // Create the crawler
            CrawlController.WebCrawlerFactory<WineCrawler> factory = () -> new WineCrawler(webService);

            // Start the crawler. This is a blocking operation for this thread
            log.info("Starting the retrieval of the HTML of the appellations...");
            crawlController.start(factory, numCrawlers);
            log.info("Finishing the retrieval of the HTML of the appellations...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void crawlLocations() {
        try {
            // Get from Web table the entries without HTML (not parsed products)
            Set<WebEntity> unparsedWebEntities = webService.findByTypeAndHtmlEntityNull(WebEntityType.location);
            int maxDepth = 0;
            int politenessDelay = 5000;
            int numCrawlers = 1;
            boolean isResumable = false;
            String storageFolder = "crawler/locations";

            // Configure the crawler
            CrawlController crawlController = prepareCrawler(maxDepth, politenessDelay, isResumable, storageFolder);

            // Add the URLs to be crawled
            unparsedWebEntities.forEach(web -> crawlController.addSeed(web.getUrl()));

            // Create the crawler
            CrawlController.WebCrawlerFactory<WineCrawler> factory = () -> new WineCrawler(webService);

            // Start the crawler. This is a blocking operation for this thread
            log.info("Starting the retrieval of the HTML of the locations...");
            crawlController.start(factory, numCrawlers);
            log.info("Finishing the retrieval of the HTML of the locations...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void crawlPairings() {
        try {
            // Get from Web table the entries without HTML (not parsed products)
            Set<WebEntity> unparsedWebEntities = webService.findByTypeAndHtmlEntityNull(WebEntityType.pairing);
            int maxDepth = 0;
            int politenessDelay = 5000;
            int numCrawlers = 1;
            boolean isResumable = false;
            String storageFolder = "crawler/pairings";

            // Configure the crawler
            CrawlController crawlController = prepareCrawler(maxDepth, politenessDelay, isResumable, storageFolder);

            // Add the URLs to be crawled
            unparsedWebEntities.forEach(web -> crawlController.addSeed(web.getUrl()));

            // Create the crawler
            CrawlController.WebCrawlerFactory<WineCrawler> factory = () -> new WineCrawler(webService);

            // Start the crawler. This is a blocking operation for this thread
            log.info("Starting the retrieval of the HTML of the pairings...");
            crawlController.start(factory, numCrawlers);
            log.info("Finishing the retrieval of the HTML of the pairings...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void crawlReviews() {
        try {
            int maxDepth = 0;
            int politenessDelay = 5000;
            int numCrawlers = 1;
            boolean isResumable = false;
            String storageFolder = "crawler/reviews";

            Pageable pageable = PageRequest.of(0, 10000);
            Page<WebEntity> unparsedWebEntities = null;
            // Get until there is no more entities
            while (unparsedWebEntities == null || unparsedWebEntities.hasNext()) {
                // Get from Web table the entries without HTML (not parsed products)
                unparsedWebEntities = webService.findByTypeAndHtmlEntityNull(WebEntityType.review, pageable);

                // Configure the crawler
                CrawlController crawlController = prepareCrawler(maxDepth, politenessDelay, isResumable, storageFolder);

                // Add the URLs to be crawled
                unparsedWebEntities.stream().forEach(web -> crawlController.addSeed(web.getUrl()));

                // Create the crawler
                CrawlController.WebCrawlerFactory<WineCrawler> factory = () -> new WineCrawler(webService);

                // Start the crawler. This is a blocking operation for this thread
                log.info("Starting the retrieval of the HTML of the reviews. {} pages left...", unparsedWebEntities.getTotalPages());
                crawlController.start(factory, numCrawlers);
                log.info("Finishing the retrieval of the HTML of the reviews for the page {}...", unparsedWebEntities.getTotalPages());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void parseDerivedProductInformation() {
        // Create the parser
        WineDerivedInfoParser wineDerivedInfoParser = new WineDerivedInfoParser(webService);

        Pageable pageable = PageRequest.of(0, 1000);
        Page<WebEntity> possibleWineHTMLs = null;
        // Get until there is no more entities
        while (possibleWineHTMLs == null || possibleWineHTMLs.hasNext()) {
            // Get the HTML of all wines. Pagination is needed to avoid heap space.
            possibleWineHTMLs = webService.findByTypeAndHtmlEntityIsNotNull(WebEntityType.wine, pageable);

            // Parse the entities
            log.info("Starting the parsing of the derived product information. Current page {} of {}...", possibleWineHTMLs.getNumber(), possibleWineHTMLs.getTotalPages());
            wineDerivedInfoParser.parse(possibleWineHTMLs);
            log.info("Finishing the parsing of the derived product information for the page {}...", possibleWineHTMLs.getNumber());

            // Go to the next page
            pageable = pageable.next();
        }
    }

    public void parseBrands() {
        // Create the parser
        BrandParser brandParser = new BrandParser(brandService);

        Pageable pageable = PageRequest.of(0, 1000);
        Page<WebEntity> possibleBrands = null;
        // Get until there is no more entities
        while (possibleBrands == null || possibleBrands.hasNext()) {
            // Get the HTML of all brands. Pagination is needed to avoid heap space.
            possibleBrands = webService.findByTypeAndHtmlEntityIsNotNull(WebEntityType.brand, pageable);

            // Parse the entities
            log.info("Starting the parsing of the brands. Current page {} of {}...", possibleBrands.getNumber(), possibleBrands.getTotalPages());
            brandParser.parse(possibleBrands);
            log.info("Finishing the parsing of the brands for the page {}...", possibleBrands.getNumber());

            // Go to the next page
            pageable = pageable.next();
        }
    }

    public void parseSimpleInformation(WebEntityType webEntityType) {
        // Create the parser
        SimpleInformationParser simpleInformationParser = new SimpleInformationParser(grapeService, appellationService,
                locationService, foodService);

        Pageable pageable = PageRequest.of(0, 1000);
        Page<WebEntity> possibleWebEntities = null;
        // Get until there is no more entities
        while (possibleWebEntities == null || possibleWebEntities.hasNext()) {
            // Get the HTML of all web entities
            possibleWebEntities = webService.findByTypeAndHtmlEntityIsNotNull(webEntityType, pageable);

            // Parse the entities
            log.info("Starting the parsing of the {}. Current page {} of {}...", webEntityType.toString(), possibleWebEntities.getNumber(), possibleWebEntities.getTotalPages());
            simpleInformationParser.parse(possibleWebEntities, webEntityType);
            log.info("Finishing the parsing of the {} for the page {}...", webEntityType.toString(), possibleWebEntities.getNumber());

            // Go to the next page
            pageable = pageable.next();
        }
    }

    public void parseReviewsAndUsers() {
        // Create the parser
        ReviewParser reviewParser = new ReviewParser(reviewService, userService);

        Pageable pageable = PageRequest.of(0, 1000);
        Page<WebEntity> possibleReviewHTMLs = null;
        // Get until there is no more entities
        while (possibleReviewHTMLs == null || possibleReviewHTMLs.hasNext()) {
            // Get the HTML of all reviews. Pagination is needed to avoid heap space.
            possibleReviewHTMLs = webService.findByTypeAndHtmlEntityIsNotNull(WebEntityType.review, pageable);

            // Parse the entities
            log.info("Starting the parsing of the reviews. Current page {} of {}...", possibleReviewHTMLs.getNumber(), possibleReviewHTMLs.getTotalPages());
            reviewParser.parse(possibleReviewHTMLs);
            log.info("Finishing the parsing of the reviews for the page {}...", possibleReviewHTMLs.getNumber());

            // Go to the next page
            pageable = pageable.next();
        }
    }

    public void parseCompetitions() {
        // Create the parser
        CompetitionParser competitionParser = new CompetitionParser(competitionService);

        Pageable pageable = PageRequest.of(0, 1000);
        Page<WebEntity> possibleWineHTMLs = null;
        // Get until there is no more entities
        while (possibleWineHTMLs == null || possibleWineHTMLs.hasNext()) {
            // Get the HTML of all wines. Pagination is needed to avoid heap space.
            possibleWineHTMLs = webService.findByTypeAndHtmlEntityIsNotNull(WebEntityType.wine, pageable);

            // Parse the entities
            log.info("Starting the parsing of the competitions. Current page {} of {}...", possibleWineHTMLs.getNumber(), possibleWineHTMLs.getTotalPages());
            competitionParser.parse(possibleWineHTMLs);
            log.info("Finishing the parsing of the competitions for the page {}...", possibleWineHTMLs.getNumber());

            // Go to the next page
            pageable = pageable.next();
        }
    }

    public void parseProducts() {
        // Create the parser
        WineParser wineParser = new WineParser(brandService, appellationService, locationService, foodService,
                reviewService, grapeService, competitionService, wineService);

        Pageable pageable = PageRequest.of(0, 1000);
        Page<WebEntity> possibleWineHTMLs = null;
        // Get until there is no more entities
        while (possibleWineHTMLs == null || possibleWineHTMLs.hasNext()) {
            // Get the HTML of all wines. Pagination is needed to avoid heap space.
            possibleWineHTMLs = webService.findByTypeAndHtmlEntityIsNotNull(WebEntityType.wine, pageable);

            // Parse the entities
            log.info("Starting the parsing of the products. Current page {} of {}...", possibleWineHTMLs.getNumber(), possibleWineHTMLs.getTotalPages());
            wineParser.parse(possibleWineHTMLs);
            log.info("Finishing the parsing of the products for the page {}...", possibleWineHTMLs.getNumber());

            // Go to the next page
            pageable = pageable.next();
        }
    }

    private CrawlController prepareCrawler(int maxDepth, int politenessDelay, boolean isResumable, String storageFolder)
            throws Exception {
        CrawlConfig config = new CrawlConfig();
        // Specify the maximum crawling depth. Crawl only the seed (product) URLs.
        config.setMaxDepthOfCrawling(maxDepth);
        // Specify a high delay (ex. 5000 ms)
        config.setPolitenessDelay(politenessDelay);
        // Set to true to include binary content
        config.setIncludeBinaryContentInCrawling(false);
        // Set to true for long crawls
        config.setResumableCrawling(isResumable);
        // Establish the user agent
        config.setUserAgentString("msnbot");
        // Set an output directory to store temporal files (needed if resumable is true)
        config.setCrawlStorageFolder(storageFolder);

        PageFetcher pageFetcher = new PageFetcher(config);
        RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);

        return new CrawlController(config, pageFetcher, robotstxtServer);
    }
}
