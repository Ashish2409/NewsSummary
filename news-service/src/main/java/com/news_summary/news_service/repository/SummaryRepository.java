package com.news_summary.news_service.repository;

import com.news_summary.news_service.models.Summary;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SummaryRepository extends MongoRepository<Summary,String> {
    
}
