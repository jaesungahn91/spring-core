package io.github.js.domain.user;

import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Embeddable;

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
