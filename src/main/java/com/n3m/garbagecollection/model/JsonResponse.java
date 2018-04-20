package com.n3m.garbagecollection.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class JsonResponse {

    private List<List<Double>> distances;
    private Info info;

}
