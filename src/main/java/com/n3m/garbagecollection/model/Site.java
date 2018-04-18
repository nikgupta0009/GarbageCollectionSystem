package com.n3m.garbagecollection.model;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Setter
@Getter
@Builder
public class Site {
    @Id
    public String id;
    private Location siteLocation;
    private String siteName;
    private Integer demandVolume;
    private boolean isCollectionRequired;
}
