package io.github.js.application.order;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
public class OrderRestController {

    @PostMapping("/orders")
    public OrderModel postOrder(@Valid @RequestBody OrderPostRequestDTO dto) {
        return OrderModel.of(dto.getMenuName(), dto.getQuantity());
    }

}
