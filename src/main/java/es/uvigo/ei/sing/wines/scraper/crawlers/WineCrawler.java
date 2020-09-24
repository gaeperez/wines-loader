package es.uvigo.ei.sing.wines.scraper.crawlers;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;
import es.uvigo.ei.sing.wines.entities.HtmlEntity;
import es.uvigo.ei.sing.wines.entities.WebEntity;
import es.uvigo.ei.sing.wines.entities.WebEntityType;
import es.uvigo.ei.sing.wines.services.WebService;
import es.uvigo.ei.sing.wines.utils.AppConstants;
import es.uvigo.ei.sing.wines.utils.AppFunctions;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

@Log4j2
@Getter
@Setter
public class WineCrawler extends WebCrawler {

    private final static Pattern EXCLUSIONS = Pattern.compile(AppConstants.CRAWLER_PATTERN_EXCLUSIONS);

    private final WebService webService;

    // Map to store the redirected urls
    private Map<String, String> mapRedirectedUrlBaseUrl;

    public WineCrawler(WebService webService) {
        this.webService = webService;

        this.mapRedirectedUrlBaseUrl = new HashMap<>();
    }

    @Override
    public boolean shouldVisit(Page referringPage, WebURL url) {
        boolean toRet = false;

        // Current URL
        String redirectedUrl = url.getURL().toLowerCase();
        // Base URL
        String baseUrl = referringPage.getWebURL().getURL().toLowerCase();

        // Only consider if the type of the url is valid and is the same domain
        if (!EXCLUSIONS.matcher(redirectedUrl).matches() && redirectedUrl.contains(referringPage.getWebURL().getDomain())) {
            log.debug("The URL {} should be visited for the referring page {}...", redirectedUrl, baseUrl);
            toRet = true;

            // Add the redirected url
            if (!redirectedUrl.equalsIgnoreCase(baseUrl))
                mapRedirectedUrlBaseUrl.put(redirectedUrl, baseUrl);
        }

        return toRet;
    }

    @Override
    public void visit(Page page) {
        // This is the redirected url
        String url = page.getWebURL().getURL();
        url = getUntilReachBaseUrl(url);
        String hash = AppFunctions.doHash(url);
        log.info("Visiting the following URL {}", url);

        // Only consider HTML documents
        if (page.getParseData() instanceof HtmlParseData) {
            HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();

            // Parse the URL and get the body
            Element bodyElement = Jsoup.parse(htmlParseData.getHtml()).body();

            // Update the Web table to include the HTML of the product
            updateWebEntity(url, hash, bodyElement);
        }
    }

    private String getUntilReachBaseUrl(String url) {
        String savedUrl = mapRedirectedUrlBaseUrl.getOrDefault(url, url);

        if (!savedUrl.equalsIgnoreCase(url))
            savedUrl = getUntilReachBaseUrl(savedUrl);

        return savedUrl;
    }

    private void updateWebEntity(String url, String hash, Element bodyElement) {
        WebEntity webEntity;

        // Get or create the web entity
        Optional<WebEntity> possibleWebEntity = webService.findByHash(hash);
        if (possibleWebEntity.isPresent()) {
            webEntity = possibleWebEntity.get();

            HtmlEntity htmlEntity = new HtmlEntity();
            // Get only the div with id page_body and apply a minify function
            if (webEntity.getType() != WebEntityType.review)
                htmlEntity.setHtml(bodyElement.getElementById("page_body").html().replaceAll("\\s+", " "));
            else
                htmlEntity.setHtml(bodyElement.html().replaceAll("\\s+", " "));

            // Add the HTML information to the existent WebEntity
            webEntity.setHtmlEntity(htmlEntity);
            // Set one to one dependency
            htmlEntity.setWeb(webEntity);

            log.debug("Saving the current URL in the database...");
            webService.save(webEntity);
        } else
            log.warn("The url {}, with hash {}, is not present in the database. Cannot assign its HTML...", url, hash);
    }
}
