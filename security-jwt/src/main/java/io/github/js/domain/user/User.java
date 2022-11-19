package io.github.js.domain.user;

import lombok.Getter;

import javax.persistence.*;

import static javax.persistence.GenerationType.*;

@Getter
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    private String email;

    @Embedded
    private Password password;

    public static User of(String email, Password password) {
        return new User(email, password);
    }

    private User(String email, Password password) {
        this.email = email;
        this.password = password;
    }

    protected User() {
    }

}
