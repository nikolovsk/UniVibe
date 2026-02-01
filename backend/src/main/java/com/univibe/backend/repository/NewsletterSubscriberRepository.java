package com.univibe.backend.repository;

import com.univibe.backend.model.NewsletterSubscriber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface NewsletterSubscriberRepository
        extends JpaRepository<NewsletterSubscriber, Long> {

    Optional<NewsletterSubscriber> findByEmail(String email);
    @Query("SELECT s FROM NewsletterSubscriber s WHERE s.active = true")
    List<NewsletterSubscriber> findAllActiveSubscribers();
}
