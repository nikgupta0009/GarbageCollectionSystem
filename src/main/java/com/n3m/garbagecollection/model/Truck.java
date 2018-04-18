package com.n3m.garbagecollection.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

@Setter
@Getter
@Builder
public class Truck {
    private Location truckLocation;
    private Integer Capacity;
    private boolean isAvailable;
}