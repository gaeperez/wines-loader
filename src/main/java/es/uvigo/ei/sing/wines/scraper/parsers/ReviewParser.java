package es.uvigo.ei.sing.wines.scraper.parsers;

import es.uvigo.ei.sing.wines.entities.ReviewEntity;
import es.uvigo.ei.sing.wines.entities.UserEntity;
import es.uvigo.ei.sing.wines.entities.WebEntity;
import es.uvigo.ei.sing.wines.services.ReviewService;
import es.uvigo.ei.sing.wines.services.UserService;
import es.uvigo.ei.sing.wines.utils.AppConstants;
import es.uvigo.ei.sing.wines.utils.AppFunctions;
import lombok.extern.log4j.Log4j2;
import org.apache.tika.utils.ExceptionUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Log4j2
public class ReviewParser {

    private final ReviewService reviewService;
    private final UserService userService;

    public ReviewParser(ReviewService reviewService, UserService userService) {
        this.reviewService = reviewService;
        this.userService = userService;
    }

    public void parse(Page<WebEntity> webReviewEntities) {
        // WebEntity variables
        String url = "", html;
        for (WebEntity webReviewEntity : webReviewEntities) {
            try {
                // Get the URL and the HTML in the WebEntity
                url = webReviewEntity.getUrl();
                html = webReviewEntity.getHtmlEntity().getHtml();
                log.info("Starting the parsing of the following review URL: {}...", url);

                // Parse the URL and get the body
                Element bodyElement = Jsoup.parse(html).body();

                // Get the elements containing the reviews information
                Elements reviewElements = bodyElement.select("div[itemprop='review']");
                if (reviewElements != null)
                    parseReview(url, reviewElements);

                log.info("Finishing the parsing of the following review URL: {}...", url);
            } catch (Exception e) {
                log.error("An error has occurred during the parsing of review: {}. See error: {}", url,
                        ExceptionUtils.getFilteredStackTrace(e));
            }
        }
    }

    private void parseReview(String url, Elements reviewElements) {
        Element element, reviewInfoElement;
        UserEntity userEntity;
        ReviewEntity reviewEntity;
        int index = 1;
        String reviewUrl, hash;

        // Iterate the reviews
        for (Element reviewElement : reviewElements) {
            // Set the current URL for this review
            reviewUrl = url + ":" + index;
            hash = AppFunctions.doHash(reviewUrl);

            // Check if the review is already inserted
            Optional<ReviewEntity> possibleReviewEntity = reviewService.findByHash(hash);
            if (!possibleReviewEntity.isPresent()) {
                // Get the user information
                element = reviewElement.selectFirst("span[itemprop='author']");
                userEntity = parseUserInformation(element);

                // Get the review information
                reviewInfoElement = reviewElement.selectFirst("div[class='opinion']");
                reviewEntity = parseReviewInformation(reviewUrl, hash, reviewInfoElement);

                // Set user-review dependency
                reviewEntity.setUser(userEntity);
                userEntity.getReviews().add(reviewEntity);

                // Save user in the database (and review by cascade)
                log.info("Saving the following user & review entities: {} & {}, in the database...", userEntity.getUserName(), reviewEntity.getSource());
                userService.save(userEntity);
            }

            index++;
        }
    }

    private UserEntity parseUserInformation(Element userElement) {
        UserEntity toRet;

        // Get the user name (unique value)
        String userName = userElement.text().trim();

        // Check in the database if the user exists
        Optional<UserEntity> possibleUserEntity = userService.findByUserName(userName);
        if (!possibleUserEntity.isPresent()) {
            toRet = new UserEntity();
            toRet.setUserName(userName);

            // Save user in the database
            log.info("Saving the following user entity: {}, in the database...", userName);
            userService.save(toRet);
        } else
            toRet = possibleUserEntity.get();

        return toRet;
    }

    private ReviewEntity parseReviewInformation(String reviewUrl, String reviewHash, Element reviewInfoElement) {
        ReviewEntity toRet;
        LocalDateTime reviewDateTime = null;
        Float reviewRating = null;
        String reviewText = "";

        // Get the datetime
        Element element = reviewInfoElement.selectFirst("p[class='date']");
        if (element != null)
            reviewDateTime = parseReviewDate(element.text().trim());

        // Get the rating
        element = reviewInfoElement.selectFirst("strong[itemprop='ratingValue']");
        if (element != null)
            reviewRating = Float.parseFloat(element.text().trim());

        // Get the text of the review
        element = reviewInfoElement.selectFirst("div[itemprop='reviewBody']");
        if (element != null)
            reviewText = element.text().trim();

        toRet = new ReviewEntity();
        toRet.setSource(reviewUrl);
        toRet.setHash(reviewHash);
        toRet.setDate(reviewDateTime);
        toRet.setRating(reviewRating);
        toRet.setText(reviewText);

        return toRet;
    }

    private LocalDateTime parseReviewDate(String dateText) {
        // Apply a regex to extract the date and the time in order to construct a LocalDateTime object
        Pattern pattern = Pattern.compile(AppConstants.REVIEW_PATTERN_DATES);
        Matcher matcher = pattern.matcher(dateText);

        // Get the first result i.e. date (e.x. 29/04/16)
        matcher.find();
        String[] date = matcher.group(1).split("/");
        // Get the second result i.e. time (e.x. 15.11)
        matcher.find();
        String[] time = matcher.group(2).split("\\.");

        // Construct the LocalDateTime (sum 2000 to the year)
        return LocalDateTime.of(2000 + Integer.parseInt(date[2]), Integer.parseInt(date[1]),
                Integer.parseInt(date[0]), Integer.parseInt(time[0]), Integer.parseInt(time[1]));
    }
}
