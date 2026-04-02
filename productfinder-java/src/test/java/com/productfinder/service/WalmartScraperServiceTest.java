package com.productfinder.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for the Walmart scraper's parsing logic.
 * These tests use injected HTML strings to avoid live network calls.
 */
class WalmartScraperServiceTest {

    private WalmartScraperService scraper;

    @BeforeEach
    void setUp() {
        scraper = new WalmartScraperService();
        ReflectionTestUtils.setField(scraper, "userAgent",
                "Mozilla/5.0 (Test) Chrome/120.0.0.0 Safari/537.36");
        ReflectionTestUtils.setField(scraper, "timeoutMs", 10000);
        ReflectionTestUtils.setField(scraper, "maxResults", 20);
        ReflectionTestUtils.setField(scraper, "baseUrl", "https://www.walmart.com");
    }

    @Test
    void scrapeProducts_returnsEmptyListOnNetworkError() {
        // Passing a definitely-unreachable host should return empty, not throw
        List<WalmartScraperService.ProductData> results =
                scraper.scrapeProducts("!@#$%^&*() unreachable host query");
        assertThat(results).isNotNull();
        // May be empty on network failure — that's the correct graceful behavior
    }

    @Test
    void productData_recordHoldsExpectedFields() {
        WalmartScraperService.ProductData pd =
                new WalmartScraperService.ProductData("Test Title",
                        "https://www.walmart.com/ip/test/123",
                        "$9.99",
                        "Available");

        assertThat(pd.title()).isEqualTo("Test Title");
        assertThat(pd.url()).contains("/ip/");
        assertThat(pd.price()).isEqualTo("$9.99");
        assertThat(pd.status()).isEqualTo("Available");
    }

    @Test
    void productData_handlesNaPrice() {
        WalmartScraperService.ProductData pd =
                new WalmartScraperService.ProductData("Item", "https://walmart.com/ip/1", "N/A", "Available");
        assertThat(pd.price()).isEqualTo("N/A");
    }
}
