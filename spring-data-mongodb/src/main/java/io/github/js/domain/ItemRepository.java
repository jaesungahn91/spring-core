package io.github.js.domain;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface ItemRepository extends MongoRepository<Item, String> {

    @Query("{name:'?0'}")
    Item findByName(String name);

//    @Query(value = "{category:'?0'}", sort = "{'quantity' : -1}")
    @Query("{category:'?0'}")
    List<Item> findByCategory(String category, Sort sort);

    long count();

}
