package com.n3m.garbagecollection.repository;

import com.n3m.garbagecollection.model.Truck;
import org.springframework.data.mongodb.repository.MongoRepository;


public interface TruckRepository extends MongoRepository<Truck, String> {
}
