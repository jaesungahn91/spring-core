package io.github.js.domain.user;

import lombok.Getter;

import javax.persistence.*;

import static java.util.Optional.ofNullable;
import static javax.persistence.GenerationType.IDENTITY;
import static org.springframework.util.StringUtils.hasText;

@Getter
@Table(name = "users")
@Entity
public class User {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Embedded
    private Email email;

    @Embedded
    private Profile profile;

    @Embedded
    private Password password;

    protected User() {
    }

    private User(Email email, UserName userName, Password password) {
        this.email = email;
        this.profile = new Profile(userName);
        this.password = password;
    }

    public static User of(Email email, UserName userName, Password password) {
        return new User(email, userName, password);
    }

    public UserName getUserName() {
        return profile.getUserName();
    }

    public void update(User updatedUser) {
        if (hasText(updatedUser.getEmail().toString())) {
            this.email = updatedUser.getEmail();
        }
    }

}
