package io.github.js.application.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.js.application.GlobalExceptionHandler;
import io.github.js.domain.user.Email;
import io.github.js.domain.user.Password;
import io.github.js.domain.user.User;
import io.github.js.domain.user.UserName;
import io.github.js.domain.user.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserRestController.class)
@Import(GlobalExceptionHandler.class)
@DisplayName("UserRestController 테스트")
class UserRestControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockitoBean UserService userService;

    @Test
    @DisplayName("POST /users: 유효성 검증 실패 시 422 반환")
    void createUser_validationFails_returns422() throws Exception {
        CreateUserRequest invalidRequest = new CreateUserRequest("not-an-email", "", "nick");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("POST /users: 정상 생성 시 201 + Location 헤더 반환")
    void createUser_success_returns201() throws Exception {
        User mockUser = User.of(new Email("test@test.com"), new UserName("nick"), new Password("pw"));
        given(userService.signUp(any())).willReturn(mockUser);

        CreateUserRequest request = new CreateUserRequest("test@test.com", "password", "nick");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"));
    }

    @Test
    @DisplayName("GET /users/{id}: 존재하지 않는 ID 조회 시 404 반환")
    void getUser_notFound_returns404() throws Exception {
        given(userService.findById(99L)).willReturn(Optional.empty());

        mockMvc.perform(get("/users/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /users/{id}: 정상 조회 시 200 + UserResponse 반환")
    void getUser_success_returns200() throws Exception {
        User mockUser = User.of(new Email("test@test.com"), new UserName("nick"), new Password("pw"));
        given(userService.findById(1L)).willReturn(Optional.of(mockUser));

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("nick"));
    }

    @Test
    @DisplayName("DELETE /users/{id}: 존재하지 않는 ID 삭제 시 404 반환 (ProblemDetail)")
    void deleteUser_notFound_returns404WithProblemDetail() throws Exception {
        org.mockito.Mockito.doThrow(new EntityNotFoundException("User not found: 99"))
                .when(userService).deleteUser(99L);

        mockMvc.perform(delete("/users/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
