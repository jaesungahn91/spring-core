package io.github.js.acceptance.user;

import io.github.js.acceptance.AcceptanceTest;
import io.github.js.application.user.CreateUserRequest;
import io.github.js.application.user.UpdateUserRequest;
import io.github.js.application.user.UserResponse;
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

    @DisplayName("유저를 생성한다.")
    @Test
    void postUser() {
        ExtractableResponse<Response> response = 유저_생성_요청();

        assertAll(
                () -> assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value()),
                () -> assertThat(response.header("Location")).isNotBlank(),
                () -> assertThat(response.as(UserResponse.class)).isNotNull()
        );
    }

    @DisplayName("유저를 조회한다.")
    @Test
    void getUser() {
        UserResponse user = 유저_생성_요청().as(UserResponse.class);

        ExtractableResponse<Response> response = 유저_조회_요청(user.id());

        assertAll(
                () -> assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value()),
                () -> assertThat(response.as(UserResponse.class).id()).isEqualTo(user.id())
        );
    }

    @DisplayName("유저를 수정한다.")
    @Test
    void putUser() {
        UserResponse user = 유저_생성_요청().as(UserResponse.class);

        ExtractableResponse<Response> response = 유저_수정_요청(user.id());

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    public static ExtractableResponse<Response> 유저_생성_요청() {
        CreateUserRequest request = new CreateUserRequest("email@email.com", "1234", "nickname");
        return RestAssured
                .given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
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
        UpdateUserRequest request = new UpdateUserRequest("updateEmail@email.com", "updateNickname", "4321");
        return RestAssured
                .given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .when().put("/users/{id}", userId)
                .then().log().all()
                .extract();
    }
}
