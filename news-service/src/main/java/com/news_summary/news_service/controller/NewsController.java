package com.news_summary.news_service.controller;

import com.news_summary.news_service.models.Summary;
import com.news_summary.news_service.service.NewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
public class NewsController {
    @Autowired
    private final NewsService newsService;

    @GetMapping("/summary")
    public Summary getNewsSummary (@RequestParam String query, @RequestParam(defaultValue = "general") String category){
        return newsService.getSummary(query,category);
    }

}
