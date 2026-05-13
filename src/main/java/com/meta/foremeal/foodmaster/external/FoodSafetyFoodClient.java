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
            @Value("${food-safety.food.base-url:http://apis.data.go.kr/1471000/FoodNtrCpntDbInfo02/getFoodNtrCpntDbInq02}") String baseUrl,
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
                .queryParam("serviceKey", apiKey)
                .queryParam("pageNo", start)
                .queryParam("numOfRows", Math.max(1, end - start + 1))
                .queryParam("type", "json")
                .queryParam("FOOD_NM_KR", foodName)
                .build()
                .toUriString();

        return restTemplate.getForObject(url, FoodSafetyFoodDto.class);
    }
}
