package com.news_summary.news_service.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "summaries")
@Data
public class Summary {
    @Id
    private String id;
    private String articleId;
    private String summaryText;
    private String summarizedBy;
    private String News;
    private LocalDateTime createdAt;
}
