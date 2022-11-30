package io.github.js.application.order;


import lombok.Getter;

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
