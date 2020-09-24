package es.uvigo.ei.sing.wines;

import es.uvigo.ei.sing.wines.controllers.AppController;
import es.uvigo.ei.sing.wines.entities.WebEntityType;
import es.uvigo.ei.sing.wines.utils.AppMenu;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

import java.util.Scanner;

@Log4j2
@SpringBootApplication
public class WinesApplication implements CommandLineRunner {

    private final AppController appController;

    public WinesApplication(AppController appController) {
        this.appController = appController;
    }

    @Override
    public void run(String... args) {
        String choice = "";
        while (!choice.equalsIgnoreCase("exit")) {
            // Show the app menu
            AppMenu.menu();

            // Get the user input
            Scanner scanner = new Scanner(System.in);
            choice = scanner.next();
            log.debug("The user choose the option number {}.", choice);

            switch (choice) {
                case "1":
                    /// Step 1: Crawl and search the list of possible wines
                    appController.crawlListOfProducts();
                    appController.crawlListUntilLastPage();
                    break;
                case "2":
                    /// Step 2: Crawl and retrieve the html of the wines
                    appController.crawlProducts();
                    break;
                case "3":
                    /// Step 3: Crawl and search the list of the brands, grapes, pairings and reviews
                    appController.parseDerivedProductInformation();
                    break;
                case "4":
                    /// Step 4: Crawl and retrieve the html of the brands, grapes, appellations, locations, pairings and reviews
                    appController.crawlBrands();
                    appController.crawlGrapes();
                    appController.crawlAppellations();
                    appController.crawlLocations();
                    appController.crawlPairings();
                    appController.crawlReviews();
                    break;
                case "4.1":
                    appController.crawlBrands();
                    break;
                case "4.2":
                    appController.crawlGrapes();
                    break;
                case "4.3":
                    appController.crawlAppellations();
                    break;
                case "4.4":
                    appController.crawlLocations();
                    break;
                case "4.5":
                    appController.crawlPairings();
                    break;
                case "4.6":
                    appController.crawlReviews();
                    break;
                case "5":
                    /// Step 5: Process the information of the retrieved brands
                    appController.parseBrands();
                    break;
                case "6":
                    /// Step 6: Process the information of the retrieved grapes
                    appController.parseSimpleInformation(WebEntityType.grape);
                    break;
                case "7":
                    /// Step 7: Process the information of the retrieved appellations
                    appController.parseSimpleInformation(WebEntityType.appellation);
                    break;
                case "8":
                    /// Step 8: Process the information of the retrieved locations
                    appController.parseSimpleInformation(WebEntityType.location);
                    break;
                case "9":
                    /// Step 9: Process the information of the retrieved pairings
                    appController.parseSimpleInformation(WebEntityType.pairing);
                    break;
                case "10":
                    /// Step 10: Process the information of the retrieved reviews and users
                    appController.parseReviewsAndUsers();
                    break;
                case "11":
                    /// Step 11: Process the information of the competitions inside each product
                    appController.parseCompetitions();
                    break;
                case "12":
                    /// Step 12: Process the information of the retrieved wines
                    appController.parseProducts();
                    break;
                case "all":
                    appController.crawlListOfProducts();
                    appController.crawlListUntilLastPage();
                    appController.crawlProducts();
                    appController.parseDerivedProductInformation();
                    appController.crawlBrands();
                    appController.crawlGrapes();
                    appController.crawlAppellations();
                    appController.crawlLocations();
                    appController.crawlPairings();
                    appController.crawlReviews();
                    appController.crawlBrands();
                    appController.crawlGrapes();
                    appController.crawlAppellations();
                    appController.crawlLocations();
                    appController.crawlPairings();
                    appController.crawlReviews();
                    appController.parseBrands();
                    appController.parseSimpleInformation(WebEntityType.grape);
                    appController.parseSimpleInformation(WebEntityType.appellation);
                    appController.parseSimpleInformation(WebEntityType.location);
                    appController.parseSimpleInformation(WebEntityType.pairing);
                    appController.parseReviewsAndUsers();
                    appController.parseCompetitions();
                    appController.parseProducts();
                    break;
                case "exit":
                    break;
                default:
                    log.warn("The input {} is not recognized by the system.", choice);
                    break;
            }
        }
    }

    public static void main(String[] args) {
        new SpringApplicationBuilder(WinesApplication.class).web(WebApplicationType.NONE).headless(false).run(args);
    }
}
