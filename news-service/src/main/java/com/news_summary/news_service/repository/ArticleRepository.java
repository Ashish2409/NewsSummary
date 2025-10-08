package com.news_summary.news_service.repository;

import com.news_summary.news_service.models.Article;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArticleRepository extends MongoRepository<Article,String> {
    List<Article> findByCategory(String category);
}
