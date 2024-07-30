package io.github.js.domain;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

@DataMongoTest
class ItemRepositoryTest {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private ItemRepository itemRepository;

    @AfterEach
    public void tearDown() {
        mongoTemplate.remove(new Query(), Item.class);
    }

    @Test
    void saveTest() {
        // given
        String expectedName = "item";
        String expectedCategory = "category";

        // when
        Item actual = itemRepository.save(new Item(expectedName, expectedCategory));

        // then
        assertAll(
                () -> assertThat(actual.getName()).isEqualTo(expectedName),
                () -> assertThat(actual.getCategory()).isEqualTo(expectedCategory),
                () -> assertThat(actual.getQuantity()).isZero()
        );
    }

    @Test
    void updateTest() {
        // given
        Item item = itemRepository.save(new Item("item", "category"));
        int expectedQuantity = 10;

        // when
        item.addQuantity(expectedQuantity);
        itemRepository.save(item);
        Item findedItem = itemRepository.findByName(item.getName());

        // then
        assertThat(findedItem.getQuantity()).isEqualTo(expectedQuantity);
    }

    @Test
    void findByNameTest() {
        // given
        String expectedName = "item";
        String expectedCategory = "category";
        itemRepository.save(new Item(expectedName, expectedCategory));

        // when
        Item actual = itemRepository.findByName(expectedName);

        // then
        assertAll(
                () -> assertThat(actual.getName()).isEqualTo(expectedName),
                () -> assertThat(actual.getCategory()).isEqualTo(expectedCategory),
                () -> assertThat(actual.getQuantity()).isZero()
        );
    }

    @Test
    void findByCategoryTest() {
        // given
        itemRepository.save(new Item("item1", 1, "category"));
        itemRepository.save(new Item("item2", 2, "category"));
        itemRepository.save(new Item("item3", 3, "category"));

        // when
        List<Item> actual = itemRepository.findByCategory("category", Sort.by(Sort.Order.desc("quantity")));

        // then
        assertAll(
                () -> assertThat(actual).hasSize(3),
                () -> assertThat(actual).extracting(Item::getQuantity).containsExactly(3, 2, 1)
        );
    }

    @Test
    void uniqueIndexTest() {
        // given
        String name = "item";
        String category = "category";
        itemRepository.save(new Item(name, category));
        Item item = new Item(name, category);

        // when & then
        assertThatThrownBy(() -> itemRepository.save(item))
                .isInstanceOf(DuplicateKeyException.class);
    }

}
