package io.github.js.domain.user;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.util.Objects;

@Embeddable
public class Password {

    @Column(name = "password", nullable = false)
    private String encodedPassword;

    protected Password() {
    }

    public Password(String encodedPassword) {
        this.encodedPassword = encodedPassword;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Password password = (Password) o;
        return Objects.equals(encodedPassword, password.encodedPassword);
    }

    @Override
    public int hashCode() {
        return Objects.hash(encodedPassword);
    }
}
