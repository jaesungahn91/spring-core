package io.github.js.acceptance.user;

import io.github.js.acceptance.AcceptanceTest;
import io.github.js.application.user.UserModel;
import io.github.js.application.user.UserPostRequestDTO;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@DisplayName("유저 관련 기능")
public class UserAcceptanceTest extends AcceptanceTest {

    @DisplayName("유저 생성를 생성한다.")
    @Test
    void postUser() {
        // given
        UserPostRequestDTO dto = new UserPostRequestDTO("email@email.com", "1234", "nickname");

        // when
        ExtractableResponse<Response> response = 유저_생성_요청(dto);

        // then
        assertAll(
                () -> assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value()),
                () -> assertThat(response.header("Location")).isNotBlank(),
                () -> assertThat(response.as(UserModel.class)).isNotNull()
        );
    }

    @DisplayName("유저를 조회한다.")
    @Test
    void getUser() {
        // given
        UserPostRequestDTO dto = new UserPostRequestDTO("email@email.com", "1234", "nickname");
        UserModel user = 유저_생성_요청(dto).as(UserModel.class);

        // when
        ExtractableResponse<Response> response = 유저_조회_요청(user);

        // then
        assertAll(
                () -> assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value()),
                () -> assertThat(response.as(UserModel.class)).isEqualTo(user)
        );

    }

    private static ExtractableResponse<Response> 유저_조회_요청(UserModel user) {
        return RestAssured
                .given().log().all()
                .when().get("/users/{id}", user.getId())
                .then().log().all()
                .extract();
    }

    public static ExtractableResponse<Response> 유저_생성_요청(UserPostRequestDTO dto) {
        return RestAssured
                .given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(dto)
                .when().post("/users")
                .then().log().all()
                .extract();
    }

}
