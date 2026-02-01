package com.univibe.backend.web;

import com.univibe.backend.repository.NewsletterSubscriberRepository;
import com.univibe.backend.service.NewsletterService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/newsletter")
@CrossOrigin(origins = "http://localhost:5173") // React
public class NewsletterController {

    private final NewsletterService service;
    private final NewsletterSubscriberRepository repository;


    public NewsletterController(NewsletterService service,
                                NewsletterSubscriberRepository repository) {
        this.service = service;
        this.repository = repository;
    }

    @PostMapping("/subscribe")
    public Map<String, String> subscribe(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String message = service.subscribe(email);
        return Map.of("message", message);
    }
    @GetMapping("/unsubscribe")
    public String unsubscribe(@RequestParam String email) {
        repository.findByEmail(email).ifPresent(sub -> {
            sub.setActive(false);
            repository.save(sub);
        });
        return "You have been unsubscribed successfully.";
    }
}

