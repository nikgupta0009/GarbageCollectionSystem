package com.n3m.garbagecollection.repository;

import com.n3m.garbagecollection.model.Site;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;


public interface SiteRepository extends MongoRepository<Site, String> {
}
