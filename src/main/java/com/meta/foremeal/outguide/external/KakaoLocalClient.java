package com.meta.foremeal.outguide.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.meta.foremeal.outguide.dto.OutGuideDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class KakaoLocalClient {

    private static final String RESTAURANT_CATEGORY_GROUP_CODE = "FD6";
    private static final int MAX_RADIUS_METERS = 20_000;
    private static final int PAGE_SIZE = 15;
    private static final int MAX_SIZE = 45;

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String apiKey;

    public KakaoLocalClient(
            RestTemplateBuilder restTemplateBuilder,
            @Value("${kakao.local.base-url:https://dapi.kakao.com}") String baseUrl,
            @Value("${kakao.local.rest-api-key:sample}") String apiKey
    ) {
        this.restTemplate = restTemplateBuilder
                .connectTimeout(Duration.ofSeconds(5))
                .readTimeout(Duration.ofSeconds(8))
                .build();
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
    }

    public List<OutGuideDto.RestaurantCandidate> searchRestaurants(
            BigDecimal lat,
            BigDecimal lng,
            int radius,
            int size
    ) {
        if (apiKey == null || apiKey.isBlank() || "sample".equalsIgnoreCase(apiKey)) {
            return sampleRestaurants(lat, lng);
        }

        int safeRadius = Math.min(Math.max(radius, 100), MAX_RADIUS_METERS);
        int safeSize = Math.min(Math.max(size, 1), MAX_SIZE);
        int maxPage = (int) Math.ceil((double) safeSize / PAGE_SIZE);
        Map<String, OutGuideDto.RestaurantCandidate> candidates = new LinkedHashMap<>();

        for (int page = 1; page <= maxPage && candidates.size() < safeSize; page++) {
            String url = UriComponentsBuilder.fromUriString(baseUrl)
                    .path("/v2/local/search/category.json")
                    .queryParam("category_group_code", RESTAURANT_CATEGORY_GROUP_CODE)
                    .queryParam("x", lng)
                    .queryParam("y", lat)
                    .queryParam("radius", safeRadius)
                    .queryParam("sort", "distance")
                    .queryParam("size", PAGE_SIZE)
                    .queryParam("page", page)
                    .build()
                    .toUriString();

            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.AUTHORIZATION, "KakaoAK " + apiKey);

            try {
                ResponseEntity<KakaoLocalResponse> response = restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        new HttpEntity<>(headers),
                        KakaoLocalResponse.class
                );

                KakaoLocalResponse body = response.getBody();
                if (body == null || body.documents() == null || body.documents().isEmpty()) {
                    break;
                }

                body.documents().stream()
                        .map(this::toCandidate)
                        .forEach(candidate -> candidates.putIfAbsent(candidate.placeId(), candidate));
            } catch (HttpClientErrorException e) {
                throw new IllegalArgumentException("Kakao Local API request failed. Check kakao.local.rest-api-key and request parameters. status=" + e.getStatusCode());
            } catch (RestClientException e) {
                throw new IllegalStateException("Failed to search nearby restaurants from Kakao Local API.", e);
            }
        }

        return candidates.values().stream()
                .limit(safeSize)
                .toList();
    }

    public List<OutGuideDto.RestaurantCandidate> searchRestaurantsByKeyword(
            String query,
            BigDecimal lat,
            BigDecimal lng,
            Integer radius,
            int size
    ) {
        if (apiKey == null || apiKey.isBlank() || "sample".equalsIgnoreCase(apiKey)) {
            String normalizedQuery = query == null ? "" : query.trim();
            return sampleRestaurants(lat, lng).stream()
                    .filter(candidate -> normalizedQuery.isBlank() || candidate.name().contains(normalizedQuery))
                .toList();
        }

        Integer safeRadius = radius == null ? null : Math.min(Math.max(radius, 100), MAX_RADIUS_METERS);
        int safeSize = Math.min(Math.max(size, 1), MAX_SIZE);
        int maxPage = (int) Math.ceil((double) safeSize / PAGE_SIZE);
        Map<String, OutGuideDto.RestaurantCandidate> candidates = new LinkedHashMap<>();

        for (int page = 1; page <= maxPage && candidates.size() < safeSize; page++) {
            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(baseUrl)
                    .path("/v2/local/search/keyword.json")
                    .queryParam("query", query)
                    .queryParam("category_group_code", RESTAURANT_CATEGORY_GROUP_CODE)
                    .queryParam("sort", "distance")
                    .queryParam("size", PAGE_SIZE)
                    .queryParam("page", page);

            if (lat != null && lng != null) {
                builder
                        .queryParam("x", lng)
                        .queryParam("y", lat);

                if (safeRadius != null) {
                    builder.queryParam("radius", safeRadius);
                }
            }

            String url = builder.build().toUriString();

            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.AUTHORIZATION, "KakaoAK " + apiKey);

            try {
                ResponseEntity<KakaoLocalResponse> response = restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        new HttpEntity<>(headers),
                        KakaoLocalResponse.class
                );

                KakaoLocalResponse body = response.getBody();
                if (body == null || body.documents() == null || body.documents().isEmpty()) {
                    break;
                }

                body.documents().stream()
                        .map(this::toCandidate)
                        .forEach(candidate -> candidates.putIfAbsent(candidate.placeId(), candidate));
            } catch (HttpClientErrorException e) {
                throw new IllegalArgumentException("Kakao Local keyword request failed. Check kakao.local.rest-api-key and request parameters. status=" + e.getStatusCode());
            } catch (RestClientException e) {
                throw new IllegalStateException("Failed to search nearby restaurants by keyword from Kakao Local API.", e);
            }
        }

        return candidates.values().stream()
                .limit(safeSize)
                .toList();
    }

    public OutGuideDto.LocationResponse searchLocation(String query) {
        if (apiKey == null || apiKey.isBlank() || "sample".equalsIgnoreCase(apiKey)) {
            BigDecimal sampleLat = new BigDecimal("36.6010");
            BigDecimal sampleLng = new BigDecimal("127.2988");
            return new OutGuideDto.LocationResponse(query, query, sampleLat, sampleLng);
        }

        String url = UriComponentsBuilder.fromUriString(baseUrl)
                .path("/v2/local/search/address.json")
                .queryParam("query", query)
                .queryParam("size", 1)
                .build()
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "KakaoAK " + apiKey);

        try {
            ResponseEntity<KakaoAddressResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    KakaoAddressResponse.class
            );

            KakaoAddressResponse body = response.getBody();
            if (body == null || body.documents() == null || body.documents().isEmpty()) {
                throw new IllegalArgumentException("주소 검색 결과가 없습니다: " + query);
            }

            KakaoAddress document = body.documents().get(0);
            return new OutGuideDto.LocationResponse(
                    query,
                    addressNameOf(document),
                    parseDecimal(document.y()),
                    parseDecimal(document.x())
            );
        } catch (HttpClientErrorException e) {
            throw new IllegalArgumentException("Kakao address request failed. Check kakao.local.rest-api-key and request parameters. status=" + e.getStatusCode());
        } catch (RestClientException e) {
            throw new IllegalStateException("Failed to search location from Kakao Local API.", e);
        }
    }

    private OutGuideDto.RestaurantCandidate toCandidate(KakaoPlace place) {
        return new OutGuideDto.RestaurantCandidate(
                place.id(),
                place.placeName(),
                place.categoryName(),
                addressOf(place),
                parseDistance(place.distance()),
                parseDecimal(place.y()),
                parseDecimal(place.x())
        );
    }

    private String addressOf(KakaoPlace place) {
        if (place.roadAddressName() != null && !place.roadAddressName().isBlank()) {
            return place.roadAddressName();
        }
        return place.addressName();
    }

    private String addressNameOf(KakaoAddress address) {
        if (address.roadAddress() != null && address.roadAddress().addressName() != null && !address.roadAddress().addressName().isBlank()) {
            return address.roadAddress().addressName();
        }
        return address.addressName();
    }

    private Integer parseDistance(String distance) {
        if (distance == null || distance.isBlank()) {
            return null;
        }
        try {
            return Integer.valueOf(distance);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private BigDecimal parseDecimal(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return new BigDecimal(value);
    }

    private List<OutGuideDto.RestaurantCandidate> sampleRestaurants(BigDecimal lat, BigDecimal lng) {
        BigDecimal baseLat = lat == null ? new BigDecimal("37.4979") : lat;
        BigDecimal baseLng = lng == null ? new BigDecimal("127.0276") : lng;

        return List.of(
                new OutGuideDto.RestaurantCandidate("sample-1", "김밥천국 강남점", "음식점 > 분식", "서울 강남구 테헤란로", 120, baseLat, baseLng),
                new OutGuideDto.RestaurantCandidate("sample-2", "홍콩반점 강남역점", "음식점 > 중식", "서울 강남구 강남대로", 180, baseLat.add(new BigDecimal("0.0010")), baseLng),
                new OutGuideDto.RestaurantCandidate("sample-3", "샐러디 역삼점", "음식점 > 샐러드", "서울 강남구 역삼로", 260, baseLat, baseLng.add(new BigDecimal("0.0012"))),
                new OutGuideDto.RestaurantCandidate("sample-4", "버거킹 강남대로점", "음식점 > 패스트푸드", "서울 강남구 강남대로", 320, baseLat.subtract(new BigDecimal("0.0011")), baseLng),
                new OutGuideDto.RestaurantCandidate("sample-5", "본죽 강남역점", "음식점 > 죽", "서울 강남구 테헤란로", 410, baseLat, baseLng.subtract(new BigDecimal("0.0010")))
        );
    }

    private record KakaoLocalResponse(List<KakaoPlace> documents) {}

    private record KakaoAddressResponse(List<KakaoAddress> documents) {}

    private record KakaoPlace(
            String id,
            @JsonProperty("place_name") String placeName,
            @JsonProperty("category_name") String categoryName,
            @JsonProperty("address_name") String addressName,
            @JsonProperty("road_address_name") String roadAddressName,
            String x,
            String y,
            String distance
    ) {}

    private record KakaoAddress(
            @JsonProperty("address_name") String addressName,
            String x,
            String y,
            @JsonProperty("road_address") KakaoRoadAddress roadAddress
    ) {}

    private record KakaoRoadAddress(
            @JsonProperty("address_name") String addressName
    ) {}
}
