package io.github.js.application.order;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import static com.fasterxml.jackson.annotation.JsonTypeInfo.As.WRAPPER_OBJECT;
import static com.fasterxml.jackson.annotation.JsonTypeInfo.Id.NAME;

@JsonTypeName("order")
@JsonTypeInfo(include = WRAPPER_OBJECT, use = NAME)
@Getter
public class OrderPostRequestDTO {

    @NotBlank
    String menuName;

    @NotNull
    Integer quantity;

    public OrderPostRequestDTO(String menuName, Integer quantity) {
        this.menuName = menuName;
        this.quantity = quantity;
    }

    protected OrderPostRequestDTO() {
    }

}
