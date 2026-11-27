package com.cinecircle.controller;

import com.cinecircle.repository.MediaItemRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final MediaItemRepository mediaItemRepository;

    public HomeController(MediaItemRepository mediaItemRepository) {
        this.mediaItemRepository = mediaItemRepository;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("mediaList", mediaItemRepository.findAll());
        return "index";
    }
}
