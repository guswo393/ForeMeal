package com.meta.foremeal.pantry.service;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class DetectedPantryItem {
    @JsonAlias({"detectedName", "label", "className"})
    private String name;

    @JsonAlias({"score", "conf"})
    private Double confidence;
}
