package com.cinecircle.controller;

import com.cinecircle.model.MediaItem;
import com.cinecircle.repository.MediaItemRepository;
import com.cinecircle.service.TmdbService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {

    private static final Logger log = LoggerFactory.getLogger(HomeController.class);

    private final MediaItemRepository mediaItemRepository;
    private final TmdbService tmdbService;

    public HomeController(MediaItemRepository mediaItemRepository, TmdbService tmdbService) {
        this.mediaItemRepository = mediaItemRepository;
        this.tmdbService = tmdbService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("mediaList", mediaItemRepository.findAll());
        return "index";
    }

    @GetMapping("/sync-posters")
    public String syncPosters() {
        List<MediaItem> items = mediaItemRepository.findAll();
        int updatedCount = 0;
        for (MediaItem item : items) {
            String posterPath = tmdbService.fetchPosterPath(item);
            if (posterPath != null && !posterPath.isBlank()) {
                mediaItemRepository.updatePosterPath(item.getId(), posterPath);
                updatedCount++;
            }
        }
        log.info("Synced TMDB posters for {}/{} media items", updatedCount, items.size());
        return "redirect:/";
    }
}
