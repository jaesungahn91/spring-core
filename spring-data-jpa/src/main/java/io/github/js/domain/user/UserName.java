package io.github.js.domain.user;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Getter
@EqualsAndHashCode(of = "nickname")
@Embeddable
public class UserName {

    @Column(nullable = false)
    private String nickname;

    protected UserName() {
    }

    public UserName(String nickname) {
        this.nickname = nickname;
    }

    @Override
    public String toString() {
        return nickname;
    }
    
}
