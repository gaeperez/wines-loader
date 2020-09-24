package es.uvigo.ei.sing.wines.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class AppConstants {

    // Crawler constants
    public static final String CRAWLER_USER_AGENT = "Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6";
    public static final String CRAWLER_REFERRER = "http://www.google.com";
    public static final int CRAWLER_DELAY_WAIT = 5000;

    // Regex patterns
    public static final String CRAWLER_PATTERN_EXCLUSIONS = ".*(\\.(css|js|xml|gif|jpg|png|mp3|mp4|zip|gz|pdf|svg))$";
    public static final String CRAWLER_PATTERN_WINE_FILTERED = ".*\\/wines:k:[a-z-]*:g:[a-z-]*:v:all:y:[0-9]*[:0-9]+$";
    public static final String CRAWLER_PATTERN_WINE_BASE = ".*\\/wines.*:v:all.*";

    // Brand constants
    public static final String BRAND_ADDRESS = "Address:";
    public static final String BRAND_CITY = "City:";
    public static final String BRAND_LOCATION = "Location:";
    public static final String BRAND_COUNTRY = "Country:";
    public static final String BRAND_PHONE = "Phone:";
    public static final String BRAND_FAX = "Fax:";
    public static final String BRAND_WEB = "Web:";

    // Review constants
    public static final String REVIEW_PATTERN_DATES = "([0-9]{1,2}/[0-9]{1,2}/[0-9]{1,2})|([0-9]{1,2}\\.[0-9]{1,2})";
}
