package com.n3m.garbagecollection.repository;

import com.n3m.garbagecollection.model.Site;
import org.springframework.data.mongodb.repository.MongoRepository;


public interface SiteRepository extends MongoRepository<Site, String> {
    Site findBySiteName(String siteName);
}
