package com.cinecircle.controller;

import com.cinecircle.model.MediaItem;
import com.cinecircle.repository.TmdbCatalogRepository;
import com.cinecircle.service.TmdbService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping("/tmdb")
public class TmdbCatalogController {

    private final TmdbService tmdbService;
    private final TmdbCatalogRepository tmdbCatalogRepository;

    public TmdbCatalogController(TmdbService tmdbService, TmdbCatalogRepository tmdbCatalogRepository) {
        this.tmdbService = tmdbService;
        this.tmdbCatalogRepository = tmdbCatalogRepository;
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
}
