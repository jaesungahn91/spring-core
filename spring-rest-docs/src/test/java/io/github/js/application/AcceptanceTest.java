package io.github.js.application;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.springframework.restdocs.restassured3.RestAssuredRestDocumentation.documentationConfiguration;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
public class AcceptanceTest {

    @LocalServerPort
    int port;

    private RequestSpecification spec;

    @BeforeEach
    public void setUp(RestDocumentationContextProvider restDocumentation) {
        RestAssured.port = port;
        this.spec = new RequestSpecBuilder()
                .addFilter(documentationConfiguration(restDocumentation))
                .build();
    }

}
