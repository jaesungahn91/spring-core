package io.github.js.acceptance.follow;

import io.github.js.acceptance.AcceptanceTest;
import io.github.js.acceptance.user.UserAcceptanceTest;
import io.github.js.application.user.UserResponse;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("팔로우 관련 기능")
public class FollowAcceptanceTest extends AcceptanceTest {

    @Test
    @DisplayName("유저를 팔로우하면 팔로잉 목록에 추가된다")
    void followUser() {
        UserResponse follower = UserAcceptanceTest.유저_생성_요청().as(UserResponse.class);
        UserResponse followee = createAnotherUser().as(UserResponse.class);

        팔로우_요청(follower.id(), followee.id());

        ExtractableResponse<Response> followings = RestAssured
                .given().when().get("/users/{id}/followings", follower.id())
                .then().extract();

        assertThat(followings.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(followings.jsonPath().getList("$")).hasSize(1);
        assertThat(followings.jsonPath().getString("[0].followeeName")).isEqualTo(followee.username());
    }

    @Test
    @DisplayName("팔로우 후 언팔로우하면 팔로잉 목록에서 제거된다")
    void unfollowUser() {
        UserResponse follower = UserAcceptanceTest.유저_생성_요청().as(UserResponse.class);
        UserResponse followee = createAnotherUser().as(UserResponse.class);

        팔로우_요청(follower.id(), followee.id());
        언팔로우_요청(follower.id(), followee.id());

        ExtractableResponse<Response> followings = RestAssured
                .given().when().get("/users/{id}/followings", follower.id())
                .then().extract();

        assertThat(followings.jsonPath().getList("$")).isEmpty();
    }

    @Test
    @DisplayName("팔로워 목록을 조회한다")
    void getFollowers() {
        UserResponse follower = UserAcceptanceTest.유저_생성_요청().as(UserResponse.class);
        UserResponse followee = createAnotherUser().as(UserResponse.class);

        팔로우_요청(follower.id(), followee.id());

        ExtractableResponse<Response> followers = RestAssured
                .given().when().get("/users/{id}/followers", followee.id())
                .then().extract();

        assertThat(followers.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(followers.jsonPath().getList("$")).hasSize(1);
        assertThat(followers.jsonPath().getString("[0].followerName")).isEqualTo(follower.username());
    }

    private ExtractableResponse<Response> createAnotherUser() {
        io.github.js.application.user.CreateUserRequest request =
                new io.github.js.application.user.CreateUserRequest(
                        "another@test.com", "pass", "anotherUser");
        return RestAssured
                .given()
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
                .body(request)
                .when().post("/users")
                .then().extract();
    }

    private void 팔로우_요청(Long followerId, Long followeeId) {
        RestAssured.given()
                .param("followerId", followerId)
                .when().post("/users/{followeeId}/follow", followeeId)
                .then().statusCode(HttpStatus.NO_CONTENT.value());
    }

    private void 언팔로우_요청(Long followerId, Long followeeId) {
        RestAssured.given()
                .param("followerId", followerId)
                .when().delete("/users/{followeeId}/follow", followeeId)
                .then().statusCode(HttpStatus.NO_CONTENT.value());
    }
}
