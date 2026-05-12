package com.meta.foremeal.foodmaster.external;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;

@Component
public class FoodSafetyFoodClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String apiKey;

    public FoodSafetyFoodClient(
            RestTemplateBuilder restTemplateBuilder,
            @Value("${food-safety.food.base-url:http://openapi.foodsafetykorea.go.kr/api}") String baseUrl,
            @Value("${food-safety.food.api-key:${food-safety.recipe.api-key:sample}}") String apiKey
    ) {
        this.restTemplate = restTemplateBuilder
                .connectTimeout(Duration.ofSeconds(5))
                .readTimeout(Duration.ofSeconds(10))
                .build();
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
    }

    public FoodSafetyFoodDto fetchByName(String foodName, int start, int end) {
        String url = UriComponentsBuilder.fromUriString(baseUrl)
                .path("/{apiKey}/I2790/json/{start}/{end}/DESC_KOR={foodName}")
                .buildAndExpand(apiKey, start, end, foodName)
                .encode()
                .toUriString();

        return restTemplate.getForObject(url, FoodSafetyFoodDto.class);
    }
}
