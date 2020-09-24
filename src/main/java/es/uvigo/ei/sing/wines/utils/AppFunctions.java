package es.uvigo.ei.sing.wines.utils;

import lombok.experimental.UtilityClass;
import lombok.extern.log4j.Log4j2;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Log4j2
@UtilityClass
public class AppFunctions {
    public static String doHash(String toHash) {
        String toRet = "";

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(toHash.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte hash : encodedHash) {
                String hex = Integer.toHexString(0xff & hash);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            toRet = hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return toRet;
    }

    public static String parseDecimalNumber(String number) {
        // Get only numbers (e.x. 91,2-95 -> 91,2 95 -> 91,2)
        number = number.replaceAll("[^\\d.,\\s]", " ").replaceAll("\\s+", " ")
                .trim().split(" ")[0];

        // Change decimal separator to dots (e.x. 91,2 -> 91.2)
        number = number.replaceAll(",", ".");

        return number;
    }

    public static String getLastPartOfURL(String url) {
        String toRet = "";

        try {
            URI uri = new URI(url);
            String path = uri.getPath();
            toRet = path.substring(path.lastIndexOf('/') + 1);
        } catch (URISyntaxException e) {
            log.error("Cannot get the last part of the URL: {}...", url);
        }

        return toRet;
    }
}
