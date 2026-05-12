package com.meta.foremeal.recipe.external;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;

@Component
public class FoodSafetyRecipeClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String apiKey;

    public FoodSafetyRecipeClient(
            RestTemplateBuilder restTemplateBuilder,
            @Value("${food-safety.recipe.base-url:http://openapi.foodsafetykorea.go.kr/api}") String baseUrl,
            @Value("${food-safety.recipe.api-key:sample}") String apiKey
    ) {
        this.restTemplate = restTemplateBuilder
                .connectTimeout(Duration.ofSeconds(5))
                .readTimeout(Duration.ofSeconds(10))
                .build();
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
    }

    public FoodSafetyRecipeDto fetch(int start, int end) {
        String url = UriComponentsBuilder.fromUriString(baseUrl)
                .pathSegment(apiKey, "COOKRCP01", "json", String.valueOf(start), String.valueOf(end))
                .build()
                .toUriString();

        return restTemplate.getForObject(url, FoodSafetyRecipeDto.class);
    }
}
