package io.github.js.domain.user;

import javax.persistence.*;

import static javax.persistence.GenerationType.IDENTITY;

@Table(name = "users")
@Entity
public class User {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Embedded
    private Email email;

    @Embedded
    private Nickname nickname;

    @Embedded
    private Password password;

    protected User() {
    }

    private User(Email email, Nickname nickname, Password password) {
        this.email = email;
        this.nickname = nickname;
        this.password = password;
    }

    public static User of(Email email, Nickname nickname, Password password) {
        return new User(email, nickname, password);
    }

    public Long getId() {
        return id;
    }

    public Email getEmail() {
        return email;
    }

    public Nickname getNickname() {
        return nickname;
    }

    public Password getPassword() {
        return password;
    }
}
