package com.news_summary.news_service.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.news_summary.news_service.models.Article;
import com.news_summary.news_service.models.Summary;
import com.news_summary.news_service.repository.ArticleRepository;
import com.news_summary.news_service.repository.SummaryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class NewsService {

    @Value("${news.api.key}")
    private String newsDataApiKey ;

    @Value("${chatlab.api.key}")
    private String chatLabApiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private SummaryRepository summaryRepository;

    public Summary getSummary(String query, String category){
        try {
            String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String startDate = LocalDate.now().minusDays(7).toString();
            String endDate = LocalDate.now().toString();

            String url = "https://gnews.io/api/v4/search?apikey=" + newsDataApiKey
                    + "&q=" + encoded
                    + "&category=" + category
                    + "&language=en"
                    + "&from=" + startDate
                    + "&to=" + endDate;

            String responseStr = restTemplate.getForObject(url, String.class);
            JsonNode response = mapper.readTree(responseStr);

            StringBuilder combinedContent = new StringBuilder();
            String firstArticleId = null;

            if (response.has("articles")) {
                for (JsonNode article : response.get("articles")) {
                    String articleId = article.path("url").asText(""); // Using URL as ID
                    String title = article.path("title").asText("");
                    String desc = article.path("description").asText("");

                    if (firstArticleId == null && !articleId.isEmpty()) {
                        firstArticleId = articleId;
                    }

                    // Append short snippet (title + first 100 chars of description)
                    combinedContent.append("- ").append(title).append(": ")
                            .append(desc.length() > 100 ? desc.substring(0, 100) + "..." : desc)
                            .append("\n");
                }
            }

            if (combinedContent.isEmpty()) {
                Summary summary = new Summary();
                summary.setSummaryText("No news found for query: " + query);
                summary.setCreatedAt(LocalDateTime.now());
                summary.setSummarizedBy("system");
                summary.setNews(responseStr);
                return summaryRepository.save(summary);
            }

            // 🔹 Step 2: Summarize via ChatLab API

            String prompt = """
            You are an expert AI news analyst.
            
            Generate a concise, well-structured summary of the most relevant news and updates 
            about '%s' in the '%s' category from the last 7 days (%s to %s).

            Format the output as follows:

            **📰 Topic:** <Topic Name>  
            **📅 Date Range:** <Start Date> to <End Date>  
            **📂 Category:** <Category>  
            **🧾 Summary (3-5 key highlights):**  
            %s

            **✅ Conclusion:** Provide a single sentence summarizing the overall trend or public sentiment.

            Ensure the tone is professional, factual, and concise.
            """.formatted(query, category, startDate, endDate, combinedContent.toString());


            String summaryText = summarizeWithChatLab(prompt);

            // 🔹 Step 3: Build and Save Summary DTO
            Summary summary = new Summary();
            summary.setArticleId(firstArticleId);
            summary.setSummaryText(summaryText);
            summary.setSummarizedBy("chatlab-llm");
            summary.setCreatedAt(LocalDateTime.now());

            return summaryRepository.save(summary);

        } catch (Exception e) {
            Summary summary = new Summary();
            summary.setSummaryText("Error occurred: " + e.getMessage());
            summary.setSummarizedBy("system");
            summary.setCreatedAt(LocalDateTime.now());
            return summaryRepository.save(summary);
        }

    }
    private String summarizeWithChatLab(String prompt) throws Exception {
        String apiUrl = "https://ai-proxy.lab.epam.com/openai/deployments/gpt-4/chat/completions?api-version=2023-08-01-preview";

        // Prepare request body
        Map<String, Object> requestBody = Map.of(
                "model", "gpt-4",
                "temperature", 0.1,
                "messages", List.of(
                        Map.of(
                                "role", "system",
                                "content", prompt
                        )
                )
        );

        // Header setup
        HttpHeaders headers = new HttpHeaders();
        headers.set("api-key", chatLabApiKey);  // Explicit
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Debug final headers and body
        System.out.println("Headers: " + headers);
        System.out.println("Request Body: " + mapper.writeValueAsString(requestBody));

        // Create an HTTP entity
        HttpEntity<String> entity = new HttpEntity<>(mapper.writeValueAsString(requestBody), headers);

        // Send request
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, entity, String.class);
            System.out.println("API Response: " + response.getBody());
            JsonNode jsonResponse = mapper.readTree(response.getBody());

            return jsonResponse.at("/choices/0/message/content").asText("No summary generated.");
        } catch (HttpClientErrorException e) {
            System.err.println("HTTP Error: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            throw e;
        }
    }
}
