package io.github.js.domain.user;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import static lombok.AccessLevel.PROTECTED;

@NoArgsConstructor(access = PROTECTED)
@EqualsAndHashCode(of = "address")
@Embeddable
public class Image {

    @Column(name = "image")
    private String address;

    public Image(String address) {
        this.address = address;
    }

}
