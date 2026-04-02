package com.productfinder.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Scrapes Walmart search results using Jsoup.
 *
 * Translated from the Python BeautifulSoup scraper (scraper.py in the MVP commit).
 * The scraper attempts two extraction strategies:
 *   1. Primary  — elements with data-item-id, matching the Walmart SERP DOM.
 *   2. Fallback — anchor tags whose href contains /ip/ (Walmart product URL pattern).
 *
 * Note: Walmart actively defends against automated access. Results may be empty
 * if they update their bot-detection. The app handles that gracefully.
 */
@Service
public class WalmartScraperService {

    private static final Logger log = LoggerFactory.getLogger(WalmartScraperService.class);

    @Value("${app.scraper.user-agent}")
    private String userAgent;

    @Value("${app.scraper.timeout-ms}")
    private int timeoutMs;

    @Value("${app.scraper.max-results}")
    private int maxResults;

    @Value("${app.scraper.base-url}")
    private String baseUrl;

    /**
     * A plain data carrier for scraped product information.
     * Used internally before persisting to the database.
     */
    public record ProductData(String title, String url, String price, String status) {}

    public List<ProductData> scrapeProducts(String query) {
        String searchUrl = buildSearchUrl(query);
        log.info("Scraping Walmart: query='{}' url='{}'", query, searchUrl);

        Document doc;
        try {
            doc = Jsoup.connect(searchUrl)
                    .userAgent(userAgent)
                    .timeout(timeoutMs)
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                    .header("Accept-Language", "en-US,en;q=0.5")
                    .header("Accept-Encoding", "gzip, deflate")
                    .header("Connection", "keep-alive")
                    .header("Upgrade-Insecure-Requests", "1")
                    .followRedirects(true)
                    .get();
        } catch (IOException e) {
            log.error("Network error while scraping Walmart for '{}': {}", query, e.getMessage());
            return List.of();
        } catch (Exception e) {
            log.error("Unexpected error while scraping Walmart for '{}': {}", query, e.getMessage());
            return List.of();
        }

        List<ProductData> products = parsePrimary(doc);
        if (products.isEmpty()) {
            log.debug("Primary extraction found nothing, trying fallback for '{}'", query);
            products = parseFallback(doc);
        }

        log.info("Scraped {} products for query '{}'", products.size(), query);
        return products;
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    private String buildSearchUrl(String query) {
        String encoded = query.trim().toLowerCase().replace(" ", "+");
        return baseUrl + "/search?q=" + encoded;
    }

    /** Primary strategy: elements marked with data-item-id attribute. */
    private List<ProductData> parsePrimary(Document doc) {
        List<ProductData> results = new ArrayList<>();
        Elements items = doc.select("[data-item-id]");

        for (Element item : items) {
            if (results.size() >= maxResults) break;
            try {
                // Title — try Walmart's known span class, then schema.org fallback
                String title = item.select("span.w_iUH7").text();
                if (title.isBlank()) title = item.select("[itemprop=name]").text();
                if (title.isBlank()) continue;

                // URL
                String href = item.select("a[href]").attr("href");
                if (href.isBlank()) continue;
                String url = href.startsWith("http") ? href : baseUrl + href;

                // Price — try known class, then schema fallback
                String price = item.select(".f_8mz.w_jlBh").text();
                if (price.isBlank()) price = item.select("[itemprop=price]").attr("content");
                if (price.isBlank()) price = "N/A";

                results.add(new ProductData(title, url, price, "Available"));
            } catch (Exception e) {
                log.debug("Skipping malformed product element: {}", e.getMessage());
            }
        }
        return results;
    }

    /**
     * Fallback strategy: anchor tags whose href contains /ip/ (Walmart product pattern).
     * Mirrors the Python fallback that scanned for links with "/ip/" in href.
     */
    private List<ProductData> parseFallback(Document doc) {
        List<ProductData> results = new ArrayList<>();
        Set<String> seen = new HashSet<>();

        Elements links = doc.select("a[href*=/ip/]");
        for (Element link : links) {
            if (results.size() >= maxResults) break;

            String href = link.attr("href");
            String url = href.startsWith("http") ? href : baseUrl + href;
            if (!seen.add(url)) continue; // de-duplicate

            String title = link.attr("aria-label");
            if (title.isBlank()) title = link.text();
            if (title.isBlank()) continue;
            if (title.length() > 200) title = title.substring(0, 200);

            results.add(new ProductData(title, url, "N/A", "Available"));
        }
        return results;
    }
}
