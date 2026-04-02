package com.productfinder.controller;

import com.productfinder.dto.SearchHistoryResponse;
import com.productfinder.dto.SearchRequest;
import com.productfinder.dto.SearchResponse;
import com.productfinder.model.User;
import com.productfinder.service.SearchService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    /**
     * POST /api/search/
     * Scrape Walmart for the given query, persist results, return them.
     */
    @PostMapping("/")
    public ResponseEntity<SearchResponse> search(@Valid @RequestBody SearchRequest req,
                                                 @AuthenticationPrincipal User user) {
        SearchResponse result = searchService.executeSearch(req, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    /**
     * GET /api/search/history?limit=20
     * Return the authenticated user's search history (newest first).
     */
    @GetMapping("/history")
    public ResponseEntity<List<SearchHistoryResponse>> history(
            @RequestParam(defaultValue = "20") int limit,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(searchService.getHistory(user, limit));
    }

    /**
     * GET /api/search/{id}
     * Return a single past search with its products.
     */
    @GetMapping("/{id}")
    public ResponseEntity<SearchResponse> getSearch(@PathVariable Long id,
                                                    @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(searchService.getSearch(id, user));
    }

    /**
     * DELETE /api/search/{id}
     * Remove a past search (cascade-deletes products).
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSearch(@PathVariable Long id,
                                             @AuthenticationPrincipal User user) {
        searchService.deleteSearch(id, user);
        return ResponseEntity.noContent().build();
    }
}
