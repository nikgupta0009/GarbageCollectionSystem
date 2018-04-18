package com.n3m.garbagecollection.model;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class Schedule {
    private Truck truck;
    private List<Site> sites;
}
