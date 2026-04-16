package io.github.js.acceptance.article;

import io.github.js.acceptance.AcceptanceTest;
import io.github.js.acceptance.user.UserAcceptanceTest;
import io.github.js.application.article.ArticleResponse;
import io.github.js.application.article.ArticleSummaryResponse;
import io.github.js.application.article.CreateArticleRequest;
import io.github.js.application.user.UserResponse;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@DisplayName("게시글 관련 기능")
public class ArticleAcceptanceTest extends AcceptanceTest {

    @Test
    @DisplayName("게시글을 생성한다")
    void createArticle() {
        UserResponse user = UserAcceptanceTest.유저_생성_요청().as(UserResponse.class);

        ExtractableResponse<Response> response = 게시글_생성_요청(user.id(), "Spring JPA", List.of("java", "spring"));

        assertAll(
                () -> assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value()),
                () -> assertThat(response.header("Location")).isNotBlank(),
                () -> assertThat(response.as(ArticleResponse.class).title()).isEqualTo("Spring JPA"),
                () -> assertThat(response.as(ArticleResponse.class).tags()).contains("java", "spring")
        );
    }

    @Test
    @DisplayName("게시글 목록을 페이지네이션으로 조회한다")
    void getArticlesWithPagination() {
        UserResponse user = UserAcceptanceTest.유저_생성_요청().as(UserResponse.class);
        게시글_생성_요청(user.id(), "Article 1", List.of());
        게시글_생성_요청(user.id(), "Article 2", List.of());
        게시글_생성_요청(user.id(), "Article 3", List.of());

        ExtractableResponse<Response> response = RestAssured
                .given().log().all()
                .param("page", 0)
                .param("size", 2)
                .when().get("/articles")
                .then().log().all()
                .extract();

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.jsonPath().getInt("totalElements")).isEqualTo(3);
        assertThat(response.jsonPath().getInt("totalPages")).isEqualTo(2);
        assertThat(response.jsonPath().getList("content")).hasSize(2);
    }

    @Test
    @DisplayName("게시글을 소프트 삭제하면 목록에서 제외된다")
    void softDeleteArticleExcludesFromList() {
        UserResponse user = UserAcceptanceTest.유저_생성_요청().as(UserResponse.class);
        ArticleResponse article = 게시글_생성_요청(user.id(), "To Be Deleted", List.of())
                .as(ArticleResponse.class);

        RestAssured.given()
                .when().delete("/articles/{id}", article.id())
                .then().statusCode(HttpStatus.NO_CONTENT.value());

        ExtractableResponse<Response> listResponse = RestAssured
                .given().when().get("/articles").then().extract();

        assertThat(listResponse.jsonPath().getInt("totalElements")).isEqualTo(0);
    }

    @Test
    @DisplayName("게시글을 조건으로 검색한다 (Specification)")
    void searchArticlesBySpecification() {
        UserResponse user = UserAcceptanceTest.유저_생성_요청().as(UserResponse.class);
        게시글_생성_요청(user.id(), "Spring Boot Guide", List.of("spring"));
        게시글_생성_요청(user.id(), "Python Tutorial", List.of("python"));

        ExtractableResponse<Response> response = RestAssured
                .given().log().all()
                .param("tag", "spring")
                .when().get("/articles/search")
                .then().log().all()
                .extract();

        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.jsonPath().getInt("totalElements")).isEqualTo(1);
        assertThat(response.jsonPath().getString("content[0].title")).isEqualTo("Spring Boot Guide");
    }

    public static ExtractableResponse<Response> 게시글_생성_요청(Long authorId, String title, List<String> tags) {
        CreateArticleRequest request = new CreateArticleRequest(title, "description", "body content", tags);
        return RestAssured
                .given().log().all()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .queryParam("authorId", authorId)
                .body(request)
                .when().post("/articles")
                .then().log().all()
                .extract();
    }
}
