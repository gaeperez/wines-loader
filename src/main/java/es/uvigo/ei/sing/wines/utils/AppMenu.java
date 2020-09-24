package es.uvigo.ei.sing.wines.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class AppMenu {

    public static void menu() {
        System.out.println("==============================");
        System.out.println("Choose one option from these choices");
        System.out.println("==============================");
        System.out.println("1 - Crawl and search the list of possible wines");
        System.out.println("2 - Crawl and retrieve the HTML of the wines");
        System.out.println("3 - Crawl and search the list of derived product information (e.g. brands, grapes, etc.)");
        System.out.println("4 - Crawl and retrieve the HTML of the derived product information (e.g. brands, grapes, etc.)");
        System.out.println("4.1 - Crawl and retrieve the HTML of only the brands");
        System.out.println("4.2 - Crawl and retrieve the HTML of only the grapes");
        System.out.println("4.3 - Crawl and retrieve the HTML of only the appellations");
        System.out.println("4.4 - Crawl and retrieve the HTML of only the locations");
        System.out.println("4.5 - Crawl and retrieve the HTML of only the pairings");
        System.out.println("4.6 - Crawl and retrieve the HTML of only the reviews");
        System.out.println("5 - Process the information of the retrieved brands");
        System.out.println("6 - Process the information of the retrieved grapes");
        System.out.println("7 - Process the information of the retrieved appellations");
        System.out.println("8 - Process the information of the retrieved locations");
        System.out.println("9 - Process the information of the retrieved pairings");
        System.out.println("10 - Process the information of the retrieved reviews and users");
        System.out.println("11 - Process the information of the competitions inside each wine");
        System.out.println("12 - Process the information of the retrieved wines");
        System.out.println("------------------------------");
        System.out.println("all - Execute all methods");
        System.out.println("exit - Close the application");
        System.out.println("==============================");
    }

}
