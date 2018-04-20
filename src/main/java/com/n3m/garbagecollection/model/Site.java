package com.n3m.garbagecollection.model;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class Site {
    public long id;
    private Location siteLocation;
    private String siteName;
    private Integer demandVolume;
    private byte[] image1;
    private byte[] image2;
    private boolean isCollectionRequired;
}
