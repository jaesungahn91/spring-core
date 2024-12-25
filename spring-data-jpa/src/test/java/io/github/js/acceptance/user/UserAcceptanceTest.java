package io.github.js.acceptance.user;

import io.github.js.acceptance.AcceptanceTest;
import io.github.js.application.user.UserModel;
import io.github.js.application.user.UserPostRequestDTO;
import io.github.js.application.user.UserPutRequestDTO;
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
        // when
        ExtractableResponse<Response> response = 유저_생성_요청();

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
        UserModel user = 유저_생성_요청().as(UserModel.class);

        // when
        ExtractableResponse<Response> response = 유저_조회_요청(user.getId());

        // then
        assertAll(
                () -> assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value()),
                () -> assertThat(response.as(UserModel.class).getId()).isEqualTo(user.getId())
        );

    }

    @DisplayName("유저를 수정한다.")
    @Test
    void putUser() {
        // given
        UserModel user = 유저_생성_요청().as(UserModel.class);

        // when
        ExtractableResponse<Response> response = 유저_수정_요청(user.getId());

        // then
        assertAll(

        );
    }

    public static ExtractableResponse<Response> 유저_생성_요청() {
        UserPostRequestDTO dto = new UserPostRequestDTO("email@email.com", "1234", "nickname");
        return RestAssured
                .given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(dto)
                .when().post("/users")
                .then().log().all()
                .extract();
    }

    public static ExtractableResponse<Response> 유저_조회_요청(long userId) {
        return RestAssured
                .given().log().all()
                .when().get("/users/{id}", userId)
                .then().log().all()
                .extract();
    }

    public static ExtractableResponse<Response> 유저_수정_요청(long userId) {
        UserPutRequestDTO dto = new UserPutRequestDTO("updateEmail@email.com", "updateNickname", "4321");
        return RestAssured
                .given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(dto)
                .when().put("/users/{id}", userId)
                .then().log().all()
                .extract();
    }

}
