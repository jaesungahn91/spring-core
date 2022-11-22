package io.github.js.application.order;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderRestController {

    @PostMapping("/orders")
    public OrderModel postOrder(OrderPostRequestDTO dto) {
        return null;
    }

}
