package com.workshop.mainserverworkshop.DB;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlugRepository extends MongoRepository<PlugSave,String> {

}
