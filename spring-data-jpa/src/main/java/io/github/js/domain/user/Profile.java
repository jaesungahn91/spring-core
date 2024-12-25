package io.github.js.domain.user;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;

import static lombok.AccessLevel.PROTECTED;

@Getter
@NoArgsConstructor(access = PROTECTED)
@Embeddable
public class Profile {

    @Embedded
    private UserName userName;

    @Column(name = "bio")
    private String bio;

    @Embedded
    private Image image;

    public Profile(UserName userName) {
        this.userName = userName;
    }

}
