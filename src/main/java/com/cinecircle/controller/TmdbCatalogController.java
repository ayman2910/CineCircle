package com.cinecircle.controller;

import com.cinecircle.model.MediaItem;
import com.cinecircle.repository.TmdbCatalogRepository;
import com.cinecircle.service.TmdbService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

@Controller
@RequestMapping("/tmdb")
public class TmdbCatalogController {

    private final TmdbService tmdbService;
    private final TmdbCatalogRepository tmdbCatalogRepository;

    public TmdbCatalogController(TmdbService tmdbService, TmdbCatalogRepository tmdbCatalogRepository) {
        this.tmdbService = tmdbService;
        this.tmdbCatalogRepository = tmdbCatalogRepository;
    }

    @GetMapping("/search")
    public String searchPage(@RequestParam(required = false) String q,
                             @RequestParam(defaultValue = "ALL") String type,
                             Model model) {
        String trimmedQuery = q != null ? q.trim() : "";
        String normalizedType = normalizeType(type);

        List<MediaItem> results = List.of();
        Set<String> existingTmdbIds = new HashSet<>();

        if (!trimmedQuery.isEmpty()) {
            results = tmdbService.searchTmdb(trimmedQuery, normalizedType);
            for (MediaItem item : results) {
                if (item.getTmdbId() != null && tmdbCatalogRepository.findByTmdbId(item.getTmdbId()).isPresent()) {
                    existingTmdbIds.add(item.getTmdbId());
                }
            }
        }

        model.addAttribute("q", trimmedQuery);
        model.addAttribute("type", normalizedType);
        model.addAttribute("results", results);
        model.addAttribute("existingTmdbIds", existingTmdbIds);
        return "tmdb/search";
    }

    @PostMapping("/import")
    public String importMedia(@RequestParam String tmdbId,
                              @RequestParam String mediaType,
                              @RequestParam(required = false) String q,
                              @RequestParam(defaultValue = "ALL") String type,
                              RedirectAttributes redirectAttributes) {
        String cleanId = tmdbId != null ? tmdbId.trim() : "";

        if (cleanId.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid TMDB item ID.");
        } else if (tmdbCatalogRepository.findByTmdbId(cleanId).isPresent()) {
            redirectAttributes.addFlashAttribute("infoMessage", "Already in CineCircle catalog.");
        } else {
            Optional<MediaItem> details = tmdbService.fetchFullMediaDetails(cleanId, mediaType);
            if (details.isPresent()) {
                MediaItem item = details.get();
                boolean inserted = tmdbCatalogRepository.insertIfNotExists(item);
                if (inserted) {
                    redirectAttributes.addFlashAttribute("successMessage",
                            "Imported '" + item.getTitle() + "' into CineCircle!");
                } else {
                    redirectAttributes.addFlashAttribute("infoMessage", "Already in CineCircle catalog.");
                }
            } else {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Could not fetch full details from TMDB. Please try again.");
            }
        }

        if (q != null && !q.isBlank()) {
            redirectAttributes.addAttribute("q", q.trim());
        }
        redirectAttributes.addAttribute("type", normalizeType(type));
        return "redirect:/tmdb/search";
    }

    @GetMapping("/bulk-import")
    @ResponseBody
    public String bulkImport(@RequestParam(defaultValue = "2") int pages) {
        List<MediaItem> fetched = tmdbService.fetchPopularCatalog(pages);
        int inserted = 0;
        int alreadyExisted = 0;

        for (MediaItem item : fetched) {
            if (tmdbCatalogRepository.insertIfNotExists(item)) {
                inserted++;
            } else {
                alreadyExisted++;
            }
        }

        return "Bulk import complete! Inserted " + inserted + " new items (" + alreadyExisted
                + " already existed). Return to /";
    }

    private String normalizeType(String type) {
        if (type == null) {
            return "ALL";
        }
        String upper = type.trim().toUpperCase(Locale.ROOT);
        if ("MOVIE".equals(upper)) {
            return "MOVIE";
        }
        if ("TV".equals(upper) || "SERIES".equals(upper)) {
            return "TV";
        }
        return "ALL";
    }
}
