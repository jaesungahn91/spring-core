package io.github.js.application.order;


import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;

import static com.fasterxml.jackson.annotation.JsonTypeInfo.As.WRAPPER_OBJECT;
import static com.fasterxml.jackson.annotation.JsonTypeInfo.Id.NAME;

@JsonTypeName("order")
@JsonTypeInfo(include = WRAPPER_OBJECT, use = NAME)
@Getter
public class OrderModel {

    private final String menuName;

    private final Integer quantity;

    public static OrderModel of(String menuName, Integer quantity) {
        return new OrderModel(menuName, quantity);
    }

    private OrderModel(String menuName, Integer quantity) {
        this.menuName = menuName;
        this.quantity = quantity;
    }

}
