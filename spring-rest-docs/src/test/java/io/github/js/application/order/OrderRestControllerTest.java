package io.github.js.application.order;

import io.github.js.application.AcceptanceTest;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.payload.JsonFieldType.NUMBER;
import static org.springframework.restdocs.payload.JsonFieldType.STRING;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.restassured3.RestAssuredRestDocumentation.document;

class OrderRestControllerTest extends AcceptanceTest {

    @Test
    void postOrder() {
        // given
        String menuName = "메뉴";

        // when
        ExtractableResponse<Response> response = 주문_생성_요청(menuName, 2);
        OrderModel order = response.as(OrderModel.class);

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(order.getMenuName()).isEqualTo(menuName);
    }

    private ExtractableResponse<Response> 주문_생성_요청(String menuName, Integer quantity) {
        final var restDocumentationFilter = document("post-order",
                requestHeaders(
                        headerWithName(AUTHORIZATION).description("Bearer token")
                ),
                requestFields(
                        fieldWithPath("order.menuName").type(STRING).description("order menu name"),
                        fieldWithPath("order.quantity").type(NUMBER).description("order quantity")
                ),
                responseFields(
                        fieldWithPath("order.menuName").type(STRING).description("order menu name"),
                        fieldWithPath("order.quantity").type(NUMBER).description("order quantity")
                )
        );

        OrderPostRequestDTO dto = new OrderPostRequestDTO(menuName, quantity);
        return RestAssured
                .given(this.spec).log().all()
                .header(AUTHORIZATION, "Bearer token")
                .accept(APPLICATION_JSON_VALUE)
                .contentType(APPLICATION_JSON_VALUE)
                .body(dto)
                .filter(restDocumentationFilter)
                .when().post("/orders")
                .then().log().all()
                .extract();
    }

}