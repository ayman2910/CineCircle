package com.cinecircle.controller;

import com.cinecircle.model.EpisodeProgress;
import com.cinecircle.model.EpisodeViewModels.SeriesTrackerDetail;
import com.cinecircle.model.MediaItem;
import com.cinecircle.model.User;
import com.cinecircle.repository.EpisodeRepository;
import com.cinecircle.repository.UserRepository;
import com.cinecircle.service.EpisodeService;
import com.cinecircle.service.EpisodeTmdbSyncService;
import com.cinecircle.service.EpisodeTmdbSyncService.SyncResult;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDate;
import java.util.Optional;

/**
 * Spring MVC controller for Feature F7 (TV Series Episode Tracker &amp; Binge-Pace Calculator).
 */
@Controller
@RequestMapping("/episodes")
public class EpisodeController {

    private final EpisodeService episodeService;
    private final EpisodeTmdbSyncService episodeTmdbSyncService;
    private final EpisodeRepository episodeRepository;
    private final UserRepository userRepository;

    public EpisodeController(EpisodeService episodeService,
                             EpisodeTmdbSyncService episodeTmdbSyncService,
                             EpisodeRepository episodeRepository,
                             UserRepository userRepository) {
        this.episodeService = episodeService;
        this.episodeTmdbSyncService = episodeTmdbSyncService;
        this.episodeRepository = episodeRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public String dashboard(@RequestParam(defaultValue = "ALL") String status,
                            Principal principal,
                            Model model) {
        User currentUser = resolveCurrentUser(principal);
        String activeStatus = episodeService.normalizeStatusFilter(status);

        model.addAttribute("trackedSeries", episodeService.getTrackedSeries(currentUser.getId(), activeStatus));
        model.addAttribute("untrackedSeries", episodeService.getUntrackedSeries(currentUser.getId()));
        model.addAttribute("activeStatus", activeStatus);
        model.addAttribute("statusCounts", episodeService.getStatusCounts(currentUser.getId()));
        return "episodes/dashboard";
    }

    @GetMapping("/{seriesId}")
    public String tracker(@PathVariable Long seriesId,
                          Principal principal,
                          Model model,
                          RedirectAttributes redirectAttributes) {
        User currentUser = resolveCurrentUser(principal);
        Optional<SeriesTrackerDetail> detailOpt = episodeService.getSeriesDetail(currentUser.getId(), seriesId);
        if (detailOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "TV series not found.");
            return "redirect:/episodes";
        }

        model.addAttribute("detail", detailOpt.get());
        return "episodes/tracker";
    }

    @PostMapping("/{seriesId}/track")
    public String startTracking(@PathVariable Long seriesId,
                                Principal principal,
                                RedirectAttributes redirectAttributes) {
        User currentUser = resolveCurrentUser(principal);
        Optional<MediaItem> seriesOpt = episodeRepository.findSeriesById(seriesId);
        if (seriesOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "TV series not found.");
            return "redirect:/episodes";
        }

        episodeService.startTracking(currentUser.getId(), seriesId);
        redirectAttributes.addFlashAttribute("successMessage",
                "Started tracking '" + seriesOpt.get().getTitle() + "'.");
        return "redirect:/episodes/" + seriesId;
    }

    @PostMapping("/{seriesId}/check")
    public String toggleEpisodeCheckpoint(@PathVariable Long seriesId,
                                          @RequestParam int seasonNumber,
                                          @RequestParam int episodeNumber,
                                          @RequestParam(defaultValue = "tracker") String returnTo,
                                          Principal principal,
                                          RedirectAttributes redirectAttributes) {
        User currentUser = resolveCurrentUser(principal);
        if (episodeRepository.findSeriesById(seriesId).isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "TV series not found.");
            return "redirect:/episodes";
        }

        episodeService.toggleEpisodeCheckpoint(currentUser.getId(), seriesId, seasonNumber, episodeNumber);
        Optional<EpisodeProgress> updated = episodeRepository.findProgressByUserAndSeries(currentUser.getId(), seriesId);
        String label = updated.map(EpisodeProgress::getCheckpointLabel).orElse("updated");
        redirectAttributes.addFlashAttribute("successMessage", "Checkpoint updated: " + label + ".");
        return resolveReturnRedirect(seriesId, returnTo);
    }

    @PostMapping("/{seriesId}/next")
    public String advanceNextEpisode(@PathVariable Long seriesId,
                                     @RequestParam(defaultValue = "tracker") String returnTo,
                                     Principal principal,
                                     RedirectAttributes redirectAttributes) {
        User currentUser = resolveCurrentUser(principal);
        if (episodeRepository.findSeriesById(seriesId).isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "TV series not found.");
            return "redirect:/episodes";
        }

        episodeService.advanceNextEpisode(currentUser.getId(), seriesId);
        Optional<EpisodeProgress> updated = episodeRepository.findProgressByUserAndSeries(currentUser.getId(), seriesId);
        String label = updated.map(EpisodeProgress::getCheckpointLabel).orElse("Not started");
        redirectAttributes.addFlashAttribute("successMessage", "Progress updated to " + label + ".");
        return resolveReturnRedirect(seriesId, returnTo);
    }

    @PostMapping("/{seriesId}/settings")
    public String updateSettings(@PathVariable Long seriesId,
                                 @RequestParam(defaultValue = "WATCHING") String watchStatus,
                                 @RequestParam(required = false)
                                 @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate targetFinishDate,
                                 Principal principal,
                                 RedirectAttributes redirectAttributes) {
        User currentUser = resolveCurrentUser(principal);
        if (episodeRepository.findSeriesById(seriesId).isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "TV series not found.");
            return "redirect:/episodes";
        }

        episodeService.updateStatusAndTargetDate(currentUser.getId(), seriesId, watchStatus, targetFinishDate);
        redirectAttributes.addFlashAttribute("successMessage", "Watch status and target finish date saved.");
        return "redirect:/episodes/" + seriesId;
    }

    @PostMapping("/{seriesId}/sync")
    public String syncFromTmdb(@PathVariable Long seriesId,
                               Principal principal,
                               RedirectAttributes redirectAttributes) {
        resolveCurrentUser(principal);
        Optional<MediaItem> seriesOpt = episodeRepository.findSeriesById(seriesId);
        if (seriesOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "TV series not found.");
            return "redirect:/episodes";
        }

        SyncResult result = episodeTmdbSyncService.syncEpisodesForSeries(seriesOpt.get());
        if (result.isSuccess()) {
            redirectAttributes.addFlashAttribute("successMessage", result.getMessage());
        } else {
            redirectAttributes.addFlashAttribute("warningMessage", result.getMessage());
        }
        return "redirect:/episodes/" + seriesId;
    }

    @PostMapping("/{seriesId}/reset")
    public String resetProgress(@PathVariable Long seriesId,
                                Principal principal,
                                RedirectAttributes redirectAttributes) {
        User currentUser = resolveCurrentUser(principal);
        if (episodeRepository.findSeriesById(seriesId).isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "TV series not found.");
            return "redirect:/episodes";
        }

        episodeService.resetProgress(currentUser.getId(), seriesId);
        redirectAttributes.addFlashAttribute("successMessage", "Progress reset to Not started.");
        return "redirect:/episodes/" + seriesId;
    }

    @PostMapping("/{seriesId}/remove")
    public String removeTracking(@PathVariable Long seriesId,
                                 Principal principal,
                                 RedirectAttributes redirectAttributes) {
        User currentUser = resolveCurrentUser(principal);
        episodeService.removeTracking(currentUser.getId(), seriesId);
        redirectAttributes.addFlashAttribute("successMessage", "Series removed from your episode tracker.");
        return "redirect:/episodes";
    }

    private User resolveCurrentUser(Principal principal) {
        if (principal == null || principal.getName() == null) {
            throw new IllegalStateException("Authenticated user not found");
        }
        return userRepository.findByEmail(UserRepository.normalizeEmail(principal.getName()))
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));
    }

    private String resolveReturnRedirect(Long seriesId, String returnTo) {
        if ("dashboard".equalsIgnoreCase(returnTo)) {
            return "redirect:/episodes";
        }
        return "redirect:/episodes/" + seriesId;
    }
}
