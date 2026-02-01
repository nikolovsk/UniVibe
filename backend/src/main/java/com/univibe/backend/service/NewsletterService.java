package com.univibe.backend.service;

import com.univibe.backend.model.NewsletterSubscriber;
import com.univibe.backend.repository.NewsletterSubscriberRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NewsletterService {

    private final NewsletterSubscriberRepository repository;
    private final MailSenderService mailSenderService;


    public NewsletterService(NewsletterSubscriberRepository repository,
                             MailSenderService mailSenderService) {
        this.repository = repository;
        this.mailSenderService = mailSenderService;
    }

    public String subscribe(String email) {
        if (repository.findByEmail(email).isPresent()) {
            return "Веќе си пријавен 🙂";
        }

        NewsletterSubscriber subscriber = new NewsletterSubscriber();
        subscriber.setEmail(email);
        repository.save(subscriber);

        String html = "<html><body>" +
                "<h2>Welcome to UniVibe 🎉</h2>" +
                "<p>Thank you for subscribing! Stay tuned for university events.</p>" +
                "<p><a href='https://yourdomain.com/api/newsletter/unsubscribe?email=" + email + "'>Unsubscribe</a></p>" +
                "</body></html>";

        mailSenderService.sendHtmlMail(email, "Welcome to UniVibe 🎓", html);

        return "Успешно се пријави 🎉";
    }

    public void sendNewEventEmail(String title, String description) {
        List<NewsletterSubscriber> subscribers = repository.findAllActiveSubscribers();
        for (NewsletterSubscriber sub : subscribers) {
            String html = "<html><body>" +
                    "<h2>New Event: " + title + "</h2>" +
                    "<p>" + description + "</p>" +
                    "<p><a href='https://yourdomain.com/api/newsletter/unsubscribe?email=" + sub.getEmail() + "'>Unsubscribe</a></p>" +
                    "</body></html>";
            mailSenderService.sendHtmlMail(sub.getEmail(), "New Event on UniVibe 🎓", html);
        }
    }
}