package es.uvigo.ei.sing.wines.scraper.parsers;

import es.uvigo.ei.sing.wines.entities.BrandEntity;
import es.uvigo.ei.sing.wines.entities.WebEntity;
import es.uvigo.ei.sing.wines.services.BrandService;
import es.uvigo.ei.sing.wines.utils.AppConstants;
import lombok.extern.log4j.Log4j2;
import org.apache.tika.utils.ExceptionUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.data.domain.Page;

import java.util.Optional;

@Log4j2
public class BrandParser {

    private final BrandService brandService;

    public BrandParser(BrandService brandService) {
        this.brandService = brandService;
    }

    public void parse(Page<WebEntity> webBrandEntities) {
        // WebEntity variables
        String url = "", html;
        // BrandEntity variables
        String brandName;
        for (WebEntity webBrandEntity : webBrandEntities) {
            try {
                // Get the URL and the HTML in the WebEntity
                url = webBrandEntity.getUrl();
                html = webBrandEntity.getHtmlEntity().getHtml();
                log.info("Starting the parsing of the following brand URL: {}...", url);

                // Parse the URL and get the body
                Element bodyElement = Jsoup.parse(html).body();

                // Get the cellar profile element
                Element cellarProfileElement = bodyElement.selectFirst("div[class='cellarprofile']");
                Element h1ProfileElement = cellarProfileElement.selectFirst("h1");
                // Get the brand name (the unique key)
                Element nameLink = h1ProfileElement.selectFirst("a");
                if (nameLink != null)
                    brandName = nameLink.text();
                else
                    brandName = h1ProfileElement.text();

                // Check if the wine is already inserted
                Optional<BrandEntity> possibleBrandEntity = brandService.findByName(brandName);
                if (!possibleBrandEntity.isPresent()) {
                    // Create the brand entity
                    BrandEntity brandEntity = new BrandEntity();
                    brandEntity.setName(brandName);

                    // Parse cellar profile information (i.e. address, location, etc.)
                    parseCellarProfileInformation(cellarProfileElement, brandEntity);

                    // Parse the brand description
                    parseBrandDescription(bodyElement, brandEntity);

                    // Save the brand in the database
                    log.info("Saving the following brand entity: {}, in the database...", brandName);
                    brandService.save(brandEntity);
                }

                log.info("Finishing the parsing of the following brand URL: {}...", url);
            } catch (Exception e) {
                log.error("An error has occurred during the parsing of brand: {}. See error: {}", url,
                        ExceptionUtils.getFilteredStackTrace(e));
            }
        }
    }

    private void parseCellarProfileInformation(Element cellarProfileElement, BrandEntity brandEntity) {
        Element descriptiveLineElement = cellarProfileElement.selectFirst("dl");
        if (descriptiveLineElement != null) {
            // Get all the DTs and DDs (should have the same size)
            Elements descriptiveTerms = descriptiveLineElement.select("dt");
            Elements descriptiveDescriptions = descriptiveLineElement.select("dd");

            if (descriptiveTerms.size() == descriptiveDescriptions.size()) {
                for (int index = 0; index < descriptiveTerms.size(); index++) {
                    Element descriptiveTerm = descriptiveTerms.get(index);
                    Element descriptiveDescription = descriptiveDescriptions.get(index);

                    String descriptiveTermText = descriptiveTerm.text().trim();
                    switch (descriptiveTermText) {
                        case AppConstants.BRAND_ADDRESS:
                            brandEntity.setAddress(descriptiveDescription.text().trim());
                            break;
                        case AppConstants.BRAND_CITY:
                            brandEntity.setCity(descriptiveDescription.text().trim());
                            break;
                        case AppConstants.BRAND_LOCATION:
                            brandEntity.setLocation(descriptiveDescription.text().trim());
                            break;
                        case AppConstants.BRAND_COUNTRY:
                            brandEntity.setCountry(descriptiveDescription.text().trim());
                            break;
                        case AppConstants.BRAND_PHONE:
                            brandEntity.setPhone(descriptiveDescription.text().trim());
                            break;
                        case AppConstants.BRAND_FAX:
                            brandEntity.setFax(descriptiveDescription.text().trim());
                            break;
                        case AppConstants.BRAND_WEB:
                            brandEntity.setWeb(descriptiveDescription.selectFirst("a").attr("href"));
                            break;
                        default:
                            log.warn("Unrecognized value {} in the parsing of the cellar profile, skipping it...", descriptiveTermText);
                            break;
                    }
                }
            } else
                log.warn("The descriptive terms and the descriptive descriptions have different size, skipping...");
        }
    }

    private void parseBrandDescription(Element bodyElement, BrandEntity brandEntity) {
        Element descriptionElement = bodyElement.selectFirst("div[class*='mini-bio']");
        if (descriptionElement != null) {
            descriptionElement = descriptionElement.selectFirst("div[class='description']");
            if (descriptionElement != null)
                brandEntity.setDescription(descriptionElement.text().trim());
        }
    }
}
