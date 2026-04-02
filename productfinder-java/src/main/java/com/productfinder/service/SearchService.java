package com.productfinder.service;

import com.productfinder.dto.SearchHistoryResponse;
import com.productfinder.dto.SearchRequest;
import com.productfinder.dto.SearchResponse;
import com.productfinder.model.Product;
import com.productfinder.model.Search;
import com.productfinder.model.User;
import com.productfinder.repository.ProductRepository;
import com.productfinder.repository.SearchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class SearchService {

    private static final Logger log = LoggerFactory.getLogger(SearchService.class);

    private final SearchRepository searchRepository;
    private final ProductRepository productRepository;
    private final WalmartScraperService scraperService;

    public SearchService(SearchRepository searchRepository,
                         ProductRepository productRepository,
                         WalmartScraperService scraperService) {
        this.searchRepository = searchRepository;
        this.productRepository = productRepository;
        this.scraperService = scraperService;
    }

    /**
     * Execute a search: scrape Walmart, persist the results, return the full response.
     */
    @Transactional
    public SearchResponse executeSearch(SearchRequest req, User user) {
        String query = req.getQuery().trim();
        log.info("User '{}' searching for: '{}'", user.getUsername(), query);

        // Persist the search record first so we have an ID
        Search search = Search.builder()
                .user(user)
                .query(query)
                .build();
        search = searchRepository.save(search);

        // Scrape Walmart (network call — errors handled inside scraper)
        List<WalmartScraperService.ProductData> scraped = scraperService.scrapeProducts(query);

        // Persist each scraped product
        final Search savedSearch = search;
        List<Product> products = scraped.stream()
                .map(pd -> Product.builder()
                        .search(savedSearch)
                        .title(pd.title())
                        .url(pd.url())
                        .price(pd.price())
                        .status(pd.status())
                        .build())
                .toList();
        productRepository.saveAll(products);

        // Re-fetch with products populated
        Search withProducts = searchRepository.findById(savedSearch.getId()).orElse(savedSearch);
        log.info("Search id={} for '{}' saved with {} products",
                withProducts.getId(), query, withProducts.getProducts().size());
        return SearchResponse.from(withProducts);
    }

    @Transactional(readOnly = true)
    public List<SearchHistoryResponse> getHistory(User user, int limit) {
        List<Search> searches = searchRepository.findByUserOrderByCreatedAtDesc(
                user, PageRequest.of(0, limit));
        return searches.stream()
                .map(SearchHistoryResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public SearchResponse getSearch(Long searchId, User user) {
        Search search = searchRepository.findByIdAndUser(searchId, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Search not found"));
        return SearchResponse.from(search);
    }

    @Transactional
    public void deleteSearch(Long searchId, User user) {
        Search search = searchRepository.findByIdAndUser(searchId, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Search not found"));
        searchRepository.delete(search);
        log.info("User '{}' deleted search id={}", user.getUsername(), searchId);
    }
}
