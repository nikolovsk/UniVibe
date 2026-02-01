package com.univibe.backend.service.impl;

import com.univibe.backend.dto.EventFilterDTO;
import com.univibe.backend.model.*;
import com.univibe.backend.repository.EventJpaRepository;
import com.univibe.backend.service.EventService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import com.univibe.backend.service.NewsletterService;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class EventServiceImpl implements EventService {
    private final EventJpaRepository eventJpaRepository;
    private final NewsletterService newsletterService;

    public EventServiceImpl(EventJpaRepository eventJpaRepository,
                            NewsletterService newsletterService) {
        this.eventJpaRepository = eventJpaRepository;
        this.newsletterService = newsletterService;
    }

    @Override
    public Event createEvent(String title, String description, LocalDateTime startDate, LocalDate endDate, String location, String image_url, Category category, EventType eventType, Faculty faculty) {
        Event event = new Event(
                title,
                description,
                startDate,
                endDate,
                location,
                image_url,
                category,
                eventType,
                faculty
        );

        event.setSource(EventSource.MANUAL);

        Event savedEvent = eventJpaRepository.save(event);

        newsletterService.sendNewEventEmail(
                savedEvent.getTitle(),
                savedEvent.getDescription()
        );

        return savedEvent;

    }

    @Override
    public Event updateEvent(Long id, String title, String description, LocalDateTime startDate, LocalDate endDate, String location, String image_url, EventMode mode, EventStatus status, EventSource source, Category category, EventType eventType, Faculty faculty) {
        Event event = this.findById(id);
        event.setTitle(title);
        event.setDescription(description);
        event.setStartDate(startDate);
        event.setEndDate(endDate);
        event.setLocation(location);
        event.setImage_url(image_url);
        event.setMode(mode);
        event.setEventType(eventType);
        event.setFaculty(faculty);
        event.setCategory(category);
        event.setStatus(status);
        event.setSource(source);
        return eventJpaRepository.save(event);
    }

    @Override
    public Event findById(Long id) {
        return eventJpaRepository.findById(id).orElse(null);
    }

    @Override
    public Event deleteEvent(Long id) {
        Event toDelete = this.findById(id);
        eventJpaRepository.delete(toDelete);
        return toDelete;
    }

    @Override
    public List<Event> findAll() {
        return eventJpaRepository.findAll();
    }

    @Override
    public List<Event> findAllByCategory(Category category) {
        return eventJpaRepository.findAllByCategory(category);
    }

    @Override
    public Page<Event> filteredEvents(EventFilterDTO filter) {
        Specification<Event> spec = Specification.where(null);

        if (filter.getKeyword() != null && !filter.getKeyword().isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("title")), "%" + filter.getKeyword().toLowerCase() + "%"),
                    cb.like(cb.lower(root.get("description")), "%" + filter.getKeyword().toLowerCase() + "%")
            ));
        }

        if (filter.getCategory() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("category"), filter.getCategory()));
        }
        if (filter.getEventType() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("eventType"), filter.getEventType()));
        }
        if (filter.getFaculty() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("faculty"), filter.getFaculty()));
        }
        if (filter.getStatus() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), filter.getStatus()));
        }
        if (filter.getMode() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("mode"), filter.getMode()));
        }
        if (filter.getLocation() != null && !filter.getLocation().isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("location"), filter.getLocation()));
        }
        if (filter.getStartDate() != null) {
            LocalDate selectedDate = filter.getStartDate();
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.and(
                            cb.isNotNull(root.get("endDate")),
                            cb.lessThanOrEqualTo(root.get("startDate").as(LocalDate.class), selectedDate),
                            cb.greaterThanOrEqualTo(root.get("endDate"), selectedDate)
                    ),
                    cb.and(
                            cb.isNull(root.get("endDate")),
                            cb.equal(root.get("startDate").as(LocalDate.class), selectedDate)
                    )
            ));
        }

        Pageable pageable = PageRequest.of(filter.getPageNumber(), filter.getPageSize(), Sort.by("startDate").descending());

        return eventJpaRepository.findAll(spec, pageable);
    }

    @Override
    public List<Event> getLatestEvents() {
        return eventJpaRepository.findAllByOrderByStartDateDesc().subList(0,3);
    }

    @Override
    public boolean existsByTitle(String title) {
        return eventJpaRepository.existsByTitle(title);
    }

    @Override
    public Event createScrapedEvent(String title, String description, LocalDateTime startDate, LocalDate endDate, String location, String image_url, Category category, EventType eventType, Faculty faculty, EventMode mode) {
        Event event = new Event(
                title,
                description,
                startDate,
                endDate,
                location,
                image_url,
                category,
                eventType,
                faculty
        );

        event.setSource(EventSource.SCRAPED);
        event.setMode(mode);

        return eventJpaRepository.save(event);
    }
}
