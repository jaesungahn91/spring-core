package io.github.js.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class ItemTest {

    @Test
    void 아이템을_생성한다() {
        // given
        String name = "item1";
        String category = "category1";

        // when
        Item item = new Item(name, category);

        // then
        assertAll(
                () -> assertThat(item.getName()).isEqualTo(name),
                () -> assertThat(item.getCategory()).isEqualTo(category),
                () -> assertThat(item.getQuantity()).isZero()
        );
    }

    @Test
    void 아이템의_수량을_더한다() {
        // given
        Item item = new Item("item1", "category1");
        int quantity = 10;

        // when
        item.addQuantity(quantity);

        // then
        assertThat(item.getQuantity()).isEqualTo(quantity);
    }

}
