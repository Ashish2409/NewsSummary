package com.news_summary.news_service.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "Articles")
public class Article {
    @Id
    private String id;
    private String title;
    private String description;
    private String category;
    private String author;
    private LocalDateTime publishedAt;
}
