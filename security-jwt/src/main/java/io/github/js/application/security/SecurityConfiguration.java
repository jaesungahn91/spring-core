package io.github.js.application.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.js.domain.jwt.JWTDeserializer;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@EnableConfigurationProperties(SecurityConfigurationProperties.class)
@RequiredArgsConstructor
@Configuration
public class SecurityConfiguration implements WebMvcConfigurer {

    private final SecurityConfigurationProperties properties;
    private final ObjectMapper objectMapper;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf().disable();
        http.cors();
        http.formLogin().disable();
        http.logout().disable();
        http.httpBasic().disable();
        http.sessionManagement().sessionCreationPolicy(STATELESS);
        http.addFilterBefore(new JWTAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
        http.authorizeRequests()
                .antMatchers(POST, "/users", "/users/login").permitAll()
                .anyRequest().authenticated();
        http.exceptionHandling()
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    sendError(response, "권한이 유효하지 않습니다.", 403);
                })
                .authenticationEntryPoint((request, response, accessDeniedException) -> {
                    sendError(response, "토큰이 유효하지 않습니다.", 401);
                });

        return http.build();
    }

    @Bean
    public JWTAuthenticationProvider jwtAuthenticationProvider(JWTDeserializer jwtDeserializer) {
        return new JWTAuthenticationProvider(jwtDeserializer);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedMethods("GET", "HEAD", "POST", "DELETE", "PUT")
                .allowedOrigins(properties.getAllowedOrigins().toArray(new String[0]))
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    private void sendError(HttpServletResponse response, String message, int status) throws IOException {
        response.setContentType(APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setStatus(status);
        response.getWriter().write(objectMapper.writeValueAsString(Map.of("message", message)));
    }

}

@ConstructorBinding
@ConfigurationProperties("security")
class SecurityConfigurationProperties {
    private final List<String> allowedOrigins;

    SecurityConfigurationProperties(List<String> allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    public List<String> getAllowedOrigins() {
        return allowedOrigins;
    }
}
