package com.meta.foremeal.pantry.service;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class PantryDetectionResponse {
    @JsonAlias({"detections", "results"})
    private List<DetectedPantryItem> items;
}
