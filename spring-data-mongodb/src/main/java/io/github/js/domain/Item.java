package io.github.js.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import static lombok.AccessLevel.PROTECTED;

@NoArgsConstructor(access = PROTECTED)
@Getter
@Document(collection = "item")
@CompoundIndexes({
        @CompoundIndex(name = "name_category_idx", def = "{ 'name': 1, 'category': 1 }", unique = true),
        @CompoundIndex(name = "name_idx", def = "{ 'name': 1}", unique = true),
})
public class Item {

    @Id
    private String id;

    private String name;
    private int quantity;
    private String category;

    protected Item(String name, String category) {
        this.name = name;
        this.quantity = 0;
        this.category = category;
    }

    protected Item(String name, int quantity, String category) {
        this.name = name;
        this.quantity = quantity;
        this.category = category;
    }

    public void addQuantity(int quantity) {
        this.quantity += quantity;
    }

}
