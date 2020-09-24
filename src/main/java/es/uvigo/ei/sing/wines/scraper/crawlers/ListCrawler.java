package es.uvigo.ei.sing.wines.scraper.crawlers;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;
import es.uvigo.ei.sing.wines.entities.HtmlEntity;
import es.uvigo.ei.sing.wines.entities.WebEntity;
import es.uvigo.ei.sing.wines.entities.WebEntityType;
import es.uvigo.ei.sing.wines.services.HtmlService;
import es.uvigo.ei.sing.wines.services.WebService;
import es.uvigo.ei.sing.wines.utils.AppConstants;
import es.uvigo.ei.sing.wines.utils.AppFunctions;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

@Log4j2
@Getter
@Setter
public class ListCrawler extends WebCrawler {

    private final static Pattern EXCLUSIONS = Pattern.compile(AppConstants.CRAWLER_PATTERN_EXCLUSIONS);
    private final static Pattern WINE_BASE = Pattern.compile(AppConstants.CRAWLER_PATTERN_WINE_BASE);

    private final WebService webService;
    private final HtmlService htmlService;

    private final boolean isLastPages;

    public ListCrawler(WebService webService, HtmlService htmlService, boolean isLastPages) {
        this.webService = webService;
        this.htmlService = htmlService;

        this.isLastPages = isLastPages;
    }

    @Override
    public boolean shouldVisit(Page referringPage, WebURL url) {
        boolean toRet = false;

        // Current URL
        String currentUrl = url.getURL().toLowerCase();
        // Base URL
        String baseUrl = referringPage.getWebURL().getURL().toLowerCase();

        // Only consider if the type of the url is valid, is the same domain and is the url is related to wines
        if (!EXCLUSIONS.matcher(currentUrl).matches() && currentUrl.contains(referringPage.getWebURL().getDomain())
                && WINE_BASE.matcher(currentUrl).matches()) {
            log.debug("The URL {} should be visited for the referring page {}...", currentUrl, baseUrl);
            toRet = true;
        }

        return toRet;
    }

    @Override
    public void visit(Page page) {
        String url = page.getWebURL().getURL().toLowerCase();
        log.info("Visiting the following URL {}", url);

        // Only consider HTML documents
        if (page.getParseData() instanceof HtmlParseData) {
            HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();

            // Parse the URL and get the body
            Element bodyElement = Jsoup.parse(htmlParseData.getHtml()).body();

            // Save the entire page
            WebEntity webEntity = new WebEntity();
            webEntity.setUrl(url);
            webEntity.setModified(LocalDateTime.now());
            webEntity.setHash(AppFunctions.doHash(url));
            webEntity.setType(WebEntityType.list);

            HtmlEntity htmlEntity = new HtmlEntity();
            // Get only the div with id page_body and apply a minify function
            htmlEntity.setHtml(bodyElement.getElementById("page_body").html().replaceAll("\\s+", " "));

            // Set one to one dependency
            webEntity.setHtmlEntity(htmlEntity);
            htmlEntity.setWeb(webEntity);

            log.debug("Saving the current URL in the database...");
            webService.saveOrGet(webEntity);

            // Find the hyperlinks for specific products
            String productUrl;
            // Get the searched results for this page
            Element productsElement = bodyElement.getElementById("search_results");
            // Get all the lists
            Elements possibleLists = productsElement.getElementsByTag("ul");
            // There is only one possible list with products
            for (Element possibleList : possibleLists) {
                // Find the list with products
                if (possibleList.hasAttr("itemscope") && possibleList.hasAttr("itemtype")) {
                    // Find product elements (the <a> inside the <h2>)
                    Elements productElements = possibleList.select("h2").select("a[href]");
                    log.info("Found {} products in this URL...", productElements.size());
                    for (Element productElement : productElements) {
                        productUrl = productElement.attr("href");

                        if (!productUrl.contains("gift-packs")) {
                            // Save it without the html (the page is not fetched yet)
                            webEntity = new WebEntity();
                            webEntity.setUrl(productUrl);
                            webEntity.setModified(LocalDateTime.now());
                            webEntity.setHash(AppFunctions.doHash(productUrl));
                            webEntity.setType(WebEntityType.wine);

                            log.debug("Saving the product URL {}...", productUrl);
                            webService.saveOrGet(webEntity);
                        } else
                            log.warn("A gift pack was found: {}, ignoring it...", productUrl);
                    }

                    // If the list of products is lesser than 20, then there is no more pages to crawl
                    if (isLastPages && productElements.size() < 20)
                        myController.shutdown();
                }
            }
        }

        log.info("Finishing with the URL {}...", url);
    }
}
